package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;
import com.github.leeonky.interpreter.InterpreterException;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.ConvertException;
import com.github.leeonky.util.ThrowingSupplier;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.leeonky.dal.ast.node.SortGroupNode.NOP_COMPARATOR;
import static com.github.leeonky.dal.runtime.ExpressionException.illegalOperation;
import static com.github.leeonky.util.Sneaky.sneakyThrow;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class Data {
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
        return resolved().value();
    }

    public Resolved resolved() {
        if (error != null)
            return sneakyThrow(error);
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
                throw new DalRuntimeException(format("Invalid input value, expect a List but: %s", dump().trim()));
            list = new DataList(context.createCollection(instance()));
        }
        return list;
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
                    return context.getConverter().convert(target, object.value());
                } catch (ConvertException convertException) {
                    e = convertException;
                }
            }
            throw e;
        });
    }

    public Data map(Function<Resolved, Object> mapper) {
        return new Data(() -> mapper.apply(resolved()), context, schemaType);
    }

    public <T, R> Data map(Function<Resolved, T> getter, Function<T, R> mapper) {
        return new Data(() -> mapper.apply(getter.apply(resolved())), context, schemaType);
    }

    public <T> Supplier<T> get(Function<Resolved, T> mapper) {
        return () -> mapper.apply(resolved());
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

    public String dump() {
        return DumpingBuffer.rootContext(context).dump(this).content();
    }

    public String dumpValue() {
        return DumpingBuffer.rootContext(context).dumpValue(this).content();
    }

    public <T> T execute(Supplier<T> supplier) {
        return context.pushAndExecute(this, supplier);
    }

    public <T> T probe(Function<Resolved, T> mapper, T defaultValue) {
        try {
            return mapper.apply(resolved());
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    public <T> Optional<T> probe(Function<Resolved, T> mapper) {
        try {
            return ofNullable(mapper.apply(resolved()));
        } catch (Throwable e) {
            return empty();
        }
    }

    public boolean probeIf(Predicate<Resolved> mapper) {
        return probe(mapper::test, false);
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
                try {
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
                } catch (InfiniteCollectionException e) {
                    throw illegalOperation("Can not sort infinite collection");
                }
            return this;
        }

        @Deprecated
        public Data wrap() {
            return new Data(() -> this, context, schemaType);
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

        public DataList list() {
            return castList().orElseThrow(() -> new DalRuntimeException(format("Invalid input value, expect a List but: %s", dump().trim())));
        }

        public Optional<DataList> castList() {
            if (list == null && isList())
                list = new DataList(context.createCollection(instance));
            return ofNullable(list);
        }

        public void eachSubData(Consumer<Data> consumer) {
            list().wraps().forEach(e -> consumer.accept(e.value()));
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

        public <T> Optional<T> cast(Class<T> type) {
            return BeanClass.cast(instance, type);
        }
    }

    public static class ResolvedMethods {

        public static Predicate<Resolved> instanceOf(Class<?> type) {
            return r -> type.isInstance(r.value());
        }

        public static <T> Function<Resolved, T> cast(Class<T> type) {
            return r -> type.cast(r.value());
        }
    }
}
