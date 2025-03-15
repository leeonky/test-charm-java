package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;
import com.github.leeonky.interpreter.InterpreterException;
import com.github.leeonky.util.ConvertException;
import com.github.leeonky.util.ThrowingSupplier;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.leeonky.dal.ast.node.SortGroupNode.NOP_COMPARATOR;
import static com.github.leeonky.dal.runtime.CurryingMethod.createCurryingMethod;
import static com.github.leeonky.util.Classes.named;
import static com.github.leeonky.util.Sneaky.sneakyThrow;
import static java.lang.String.format;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

//TODO use generic
public class Data {
    public static class ResolvedMethods {
        public static Predicate<Resolved> instanceOf(Class<?> type) {
            return r -> type.isInstance(r.value());
        }
    }

    public class Resolved {
        private final Object instance;
        private DataList list;

        public Resolved(Object instance) {
            this.instance = instance;
        }

        @SuppressWarnings("unchecked")
        public <T> T value() {
            return (T) instance;
        }

        public boolean isNull() {
            return context.isNull(instance);
        }

        public boolean isList() {
            return context.isRegisteredList(instance) || (instance != null && instance.getClass().isArray());
        }

        public DataList asList() {
            if (list == null) {
                if (!isList())
                    throw new DalRuntimeException(format("Invalid input value, expect a List but: %s", dumpAll().trim()));
                list = new DataList(context.createCollection(instance));
            }
            return list;
        }

        public void eachSubData(Consumer<Data> consumer) {
            asList().wraps().forEach(e -> consumer.accept(e.value()));
        }

        public boolean instanceOf(Class<?> type) {
            return type.isInstance(instance);
        }

        public Data getValue(Object field) {
            return Data.this.getValue(field);
        }

        public Set<?> fieldNames() {
            return context.findPropertyReaderNames(instance);
        }

        public Data repack() {
            return Data.this;
        }

        boolean isEnum() {
            return value() != null && value().getClass().isEnum();
        }
    }

    private final SchemaType schemaType;
    private final DALRuntimeContext context;
    private final ThrowingSupplier<?> supplier;
    private DataList list;
    private Function<Throwable, Throwable> errorMapper = e -> e;
    private final boolean isListMapping;
    private Resolved resolved;

    private Throwable error;

    public Data(ThrowingSupplier<?> supplier, DALRuntimeContext context, SchemaType schemaType) {
        this.supplier = Objects.requireNonNull(supplier);
        this.context = context;
        this.schemaType = schemaType;
        isListMapping = false;
    }

    public Data(ThrowingSupplier<?> supplier, DALRuntimeContext context, SchemaType schemaType, boolean isListMapping) {
        this.supplier = Objects.requireNonNull(supplier);
        this.context = context;
        this.schemaType = schemaType;
        this.isListMapping = isListMapping;
    }

    public Data onError(Function<Throwable, Throwable> mapper) {
        errorMapper = errorMapper.andThen(mapper);
        return this;
    }

    public Object instance() {
        return error != null ? sneakyThrow(error) : resolved().value();
    }

    public Resolved resolved() {
        if (resolved == null) {
            try {
                resolved = new Resolved(supplier.get());
            } catch (Throwable e) {
                sneakyThrow(error = errorMapper.apply(e));
            }
            handlers.accept(resolved);
        }
        return resolved;
    }

    //    TODO lazy
    @Deprecated
    public Set<?> fieldNames() {
        return context.findPropertyReaderNames(instance());
    }

    //    TODO lazy
    @Deprecated
    public boolean isList() {
        return resolved().isList();
    }

    //    TODO lazy
    @Deprecated
    public DataList list() {
        if (list == null) {
            if (!isList())
                throw new DalRuntimeException(format("Invalid input value, expect a List but: %s", dumpAll().trim()));
            list = new DataList(context.createCollection(instance()));
        }
        return list;
    }

    //    TODO lazy
    @Deprecated
    public boolean isNull() {
        return context.isNull(instance());
    }

    public Data getValue(List<Object> propertyChain) {
        return propertyChain.isEmpty() ? this :
                getValue(propertyChain.get(0)).getValue(propertyChain.subList(1, propertyChain.size()));
    }

    public Data getValue(Object propertyChain) {
        List<Object> chain = schemaType.access(propertyChain).getPropertyChainBefore(schemaType);
        if (chain.size() == 1 && chain.get(0).equals(propertyChain)) {
            ThrowingSupplier<?> supplier = () -> {
                try {
                    return isList() ? fetchFromList(propertyChain) : context.getPropertyValue(this, propertyChain);
                } catch (IndexOutOfBoundsException ex) {
                    throw new DalRuntimeException(ex.getMessage());
                } catch (ListMappingElementAccessException | ExpressionException | InterpreterException ex) {
                    throw ex;
                } catch (Throwable e) {
                    throw buildExceptionWithComments(propertyChain, e);
                }
            };
            boolean isSubListMapping;
            if (isListMapping) {
                isSubListMapping = propertyChain instanceof String;
            } else {
                isSubListMapping = false;
            }
            return new Data(supplier, context, propertySchema(propertyChain, isSubListMapping), isSubListMapping);
        }
        return getValue(chain);
    }

    private static DalRuntimeException buildExceptionWithComments(Object propertyChain, Throwable e) {
        return new DalRuntimeException(format("Get property `%s` failed, property can be:\n" +
                "  1. public field\n" +
                "  2. public getter\n" +
                "  3. public method\n" +
                "  4. Map key value\n" +
                "  5. customized type getter\n" +
                "  6. static method extension", propertyChain), e);
    }

    private Object fetchFromList(Object property) {
        return property instanceof String ? context.getPropertyValue(this, property) :
                list().getByIndex((int) property);
    }

    public SchemaType propertySchema(Object property, boolean isListMapping) {
        return isListMapping ? schemaType.mappingAccess(property) : schemaType.access(property);
    }

    public Object firstFieldFromAlias(Object alias) {
        return schemaType.firstFieldFromAlias(alias);
    }

    public Data convert(Class<?>... targets) {
        return map(object -> {
            ConvertException e = null;
            for (Class<?> target : targets) {
                try {
                    return context.getConverter().convert(target, object);
                } catch (ConvertException convertException) {
                    e = convertException;
                }
            }
            throw e;
        });
    }

    //    use lazy in mapper
    @Deprecated
    public Data map(Function<Object, Object> mapper) {
        return new Data(() -> mapper.apply(instance()), context, schemaType);
    }

    public Data filter(String prefix) {
        return new Data(() -> {
            FilteredObject filteredObject = new FilteredObject();
            fieldNames().stream().filter(String.class::isInstance).map(String.class::cast)
                    .filter(field -> field.startsWith(prefix)).forEach(fieldName ->
                            filteredObject.put(fieldName.substring(prefix.length()), getValue(fieldName).instance()));
            return filteredObject;
        }, context, schemaType);
    }

    //TODO move to lazy obj
    @Deprecated
    public String dumpAll() {
        return DumpingBuffer.rootContext(context).dump(this).content();
    }

    //TODO move to lazy obj
    public String dumpValue() {
        return DumpingBuffer.rootContext(context).dumpValue(this).content();
    }

    public <T> T execute(Supplier<T> supplier) {
        return context.pushAndExecute(this, supplier);
    }

    //TODO move to lazy obj
    @Deprecated
    public Optional<CurryingMethod> currying(Object property) {
        return currying(instance(), property);
    }

    //TODO move to lazy obj
    @Deprecated
    private Optional<CurryingMethod> currying(Object instance, Object property) {
        List<InstanceCurryingMethod> methods = context.methodToCurrying(named(instance.getClass()), property).stream()
                .map(method -> createCurryingMethod(instance, method, context.getConverter(), context)).collect(toList());
        if (!methods.isEmpty())
            return of(new CurryingMethodGroup(methods, null));
        return context.getImplicitObject(instance).flatMap(obj -> currying(obj, property));
    }

    //TODO move to lazy obj
    @Deprecated
    public boolean instanceOf(Class<?> type) {
        try {
            return type.isInstance(instance());
        } catch (Exception e) {
            return false;
        }
    }

    public Data resolve() {
        instance();
        return this;
    }

    private Consumer<Resolved> handlers = r -> {
    };

    public Data peek(Consumer<Resolved> peek) {
        if (resolved != null)
            peek.accept(resolved);
        else
            handlers = handlers.andThen(peek);
        return this;
    }

    public Data trigger(Data another) {
        return peek(r -> another.resolve());
    }

    public ConditionalAction when(Predicate<Resolved> condition) {
        return new ConditionalAction(condition);
    }

    public class ConditionalAction {
        private final Predicate<Resolved> condition;

        public ConditionalAction(Predicate<Resolved> condition) {
            this.condition = condition;
        }

        public Data then(Consumer<Resolved> then) {
            peek(r -> {
                if (condition.test(r))
                    then.accept(r);
            });
            return Data.this;
        }

        public Data thenThrow(Supplier<Throwable> supplier) {
            peek(r -> {
                if (condition.test(r))
                    sneakyThrow(supplier.get());
            });
            return Data.this;
        }
    }

    static class FilteredObject extends LinkedHashMap<String, Object> implements PartialObject {
    }

    public class DataList extends DALCollection.Decorated<Object> {
        public DataList(DALCollection<Object> origin) {
            super(origin);
        }

        public DALCollection<Data> wraps() {
            return map((index, e) -> new Data(() -> e, context, schemaType.access(index)));
        }

        public AutoMappingList listMap(Function<Data, Data> mapper) {
            return new AutoMappingList(mapper, wraps());
        }

        public DataList sort(Comparator<Data> comparator) {
            if (comparator != NOP_COMPARATOR)
                return new DataList(new CollectionDALCollection<Object>(wraps().collect().stream()
                        .sorted(comparator).map(Data::instance).collect(toList())) {
                    @Override
                    public int firstIndex() {
                        return DataList.this.firstIndex();
                    }

                    @Override
                    public boolean infinite() {
                        return DataList.this.infinite();
                    }
                });
            return this;
        }

        @Deprecated
        public Data wrap() {
            return new Data(() -> this, context, schemaType);
        }
    }
}
