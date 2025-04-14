package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;
import com.github.leeonky.interpreter.InterpreterException;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.ConvertException;
import com.github.leeonky.util.ThrowingSupplier;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.leeonky.dal.ast.node.SortGroupNode.NOP_COMPARATOR;
import static com.github.leeonky.dal.runtime.DalException.buildUserRuntimeException;
import static com.github.leeonky.dal.runtime.ExpressionException.illegalOperation;
import static com.github.leeonky.util.Sneaky.sneakyThrow;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class Data<T> {
    private final SchemaType schemaType;
    private final DALRuntimeContext context;
    private final T value;
    private DataList list;

    public Data(T value, DALRuntimeContext context, SchemaType schemaType) {
        this.context = context;
        this.schemaType = schemaType;
        this.value = value;
    }

    public T instance() {
        return value;
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
                throw new DalRuntimeException(format("Invalid input value, expect a List but: %s", dump().trim()));
            list = new DataList(context.createCollection(instance()));
        }
        return list;
    }

    public boolean isNull() {
        return context.isNull(instance());
    }

    public Data<?> getValue(List<Object> propertyChain) {
        return propertyChain.isEmpty() ? this :
                getValue(propertyChain.get(0)).getValue(propertyChain.subList(1, propertyChain.size()));
    }

    public Data<?> getValue(Object propertyChain) {
        List<Object> chain = schemaType.access(propertyChain).getPropertyChainBefore(schemaType);
        if (chain.size() == 1 && chain.get(0).equals(propertyChain)) {
            try {
                Object value = isList() && !(propertyChain instanceof String) ? list().getByIndex((int) propertyChain)
                        : context.getPropertyValue(this, propertyChain);
                return new Data<>(value, context, propertySchema(propertyChain,
                        this.value instanceof AutoMappingList && propertyChain instanceof String));
            } catch (IndexOutOfBoundsException ex) {
                throw new DalRuntimeException(ex.getMessage());
            } catch (ListMappingElementAccessException | ExpressionException | InterpreterException ex) {
                throw ex;
            } catch (Throwable e) {
                throw new DalRuntimeException(format("Get property `%s` failed, property can be:\n" +
                        "  1. public field\n" +
                        "  2. public getter\n" +
                        "  3. public method\n" +
                        "  4. Map key value\n" +
                        "  5. customized type getter\n" +
                        "  6. static method extension", propertyChain), e);
            }
        }
        return getValue(chain);
    }

    public SchemaType propertySchema(Object property, boolean isListMapping) {
        return isListMapping ? schemaType.mappingAccess(property) : schemaType.access(property);
    }

    public Object firstFieldFromAlias(Object alias) {
        return schemaType.firstFieldFromAlias(alias);
    }

    public Data<?> convert(Class<?>... targets) {
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

    public <N> Data<N> map(Function<T, N> mapper) {
        return new Data<>(mapper.apply(instance()), context, schemaType);
    }

    public Data<?> filter(String prefix) {
        FilteredObject filteredObject = new FilteredObject();
        fieldNames().stream().filter(String.class::isInstance).map(String.class::cast)
                .filter(field -> field.startsWith(prefix)).forEach(fieldName ->
                        filteredObject.put(fieldName.substring(prefix.length()), getValue(fieldName).instance()));
        return new Data<>(filteredObject, context, schemaType);
    }

    public String dump() {
        return DumpingBuffer.rootContext(context).dump(this).content();
    }

    public String dumpValue() {
        return DumpingBuffer.rootContext(context).dumpValue(this).content();
    }

    public <N> N execute(Supplier<N> supplier) {
        return context.pushAndExecute(this, supplier);
    }

    public <N> Optional<N> cast(Class<N> type) {
        return BeanClass.cast(instance(), type);
    }

    public boolean instanceOf(Class<?> type) {
        try {
            return type.isInstance(instance());
        } catch (Throwable ignore) {
            return false;
        }
    }

    public static <N> Data<N> lazy(ThrowingSupplier<N> supplier, DALRuntimeContext context, SchemaType schemaType) {
        try {
            return new Data<>(supplier.get(), context, schemaType);
        } catch (Throwable e) {
            return new Data<N>(null, context, schemaType) {
                @Override
                public N instance() {
                    return sneakyThrow(buildUserRuntimeException(e));
                }
            };
        }
    }

    public class DataList extends DALCollection.Decorated<Object> {
        public DataList(DALCollection<Object> origin) {
            super(origin);
        }

        public DALCollection<Data<?>> wraps() {
            return map((index, e) -> new Data<>(e, context, schemaType.access(index)));
        }

        public AutoMappingList autoMapping(Function<Data<?>, Data<?>> mapper) {
            return new AutoMappingList(mapper, wraps());
        }

        public DataList sort(Comparator<Data<?>> comparator) {
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

        public Data<?> wrap() {
            return new Data<>(this, context, schemaType);
        }
    }
}
