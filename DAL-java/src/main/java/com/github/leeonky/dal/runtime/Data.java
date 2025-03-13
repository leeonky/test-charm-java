package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;
import com.github.leeonky.interpreter.InterpreterException;
import com.github.leeonky.util.ConvertException;
import com.github.leeonky.util.Sneaky;
import com.github.leeonky.util.ThrowingSupplier;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.leeonky.dal.ast.node.SortGroupNode.NOP_COMPARATOR;
import static com.github.leeonky.dal.runtime.CurryingMethod.createCurryingMethod;
import static com.github.leeonky.util.Classes.named;
import static java.lang.String.format;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

//TODO use generic
public class Data {
    private final SchemaType schemaType;
    private final DALRuntimeContext context;
    private final ThrowingSupplier<?> supplier;
    private Object instance;
    private boolean resolved = false;
    private DataList list;
    private Throwable error;
    private Function<Throwable, Throwable> errorMapper = e -> e;
    private final List<Consumer<Object>> consumers = new ArrayList<>();
    private final boolean isListMapping;

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

    public Data mapError(Function<Throwable, Throwable> mapper) {
        errorMapper = mapper;
        return this;
    }

    public Object instance() {
        if (error != null)
            return Sneaky.sneakyThrow(error);
        if (resolved)
            return instance;
        try {
            instance = supplier.get();
            resolved = true;
            for (Consumer<Object> f : consumers)
                f.accept(instance);
            return instance;
        } catch (Throwable e) {
            return Sneaky.sneakyThrow(error = errorMapper.apply(e));
        }
    }

    public Set<?> fieldNames() {
        return context.findPropertyReaderNames(instance());
    }

    public boolean isList() {
        Object instance = instance();
        return context.isRegisteredList(instance) || (instance != null && instance.getClass().isArray());
    }

    public DataList list() {
        if (list == null) {
            if (!isList())
                throw new DalRuntimeException(format("Invalid input value, expect a List but: %s", dumpAll().trim()));
            list = new DataList(context.createCollection(instance()));
        }
        return list;
    }

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

    public String dumpAll() {
        return DumpingBuffer.rootContext(context).dump(this).content();
    }

    public String dumpValue() {
        return DumpingBuffer.rootContext(context).dumpValue(this).content();
    }

    public <T> T execute(Supplier<T> supplier) {
        return context.pushAndExecute(this, supplier);
    }

    public Optional<CurryingMethod> currying(Object property) {
        return currying(instance(), property);
    }

    private Optional<CurryingMethod> currying(Object instance, Object property) {
        List<InstanceCurryingMethod> methods = context.methodToCurrying(named(instance.getClass()), property).stream()
                .map(method -> createCurryingMethod(instance, method, context.getConverter(), context)).collect(toList());
        if (!methods.isEmpty())
            return of(new CurryingMethodGroup(methods, null));
        return context.getImplicitObject(instance).flatMap(obj -> currying(obj, property));
    }

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

    public Data peek(Consumer<Object> consumer) {
        if (resolved)
            consumer.accept(instance);
        else
            consumers.add(consumer);
        return this;
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
            if (comparator != NOP_COMPARATOR) {
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
            }
            return this;
        }

        @Deprecated
        public Data wrap() {
            return new Data(() -> this, context, schemaType);
        }
    }
}
