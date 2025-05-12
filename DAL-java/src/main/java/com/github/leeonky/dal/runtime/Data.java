package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;
import com.github.leeonky.interpreter.InterpreterException;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.ConvertException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.leeonky.dal.ast.node.SortGroupNode.NOP_COMPARATOR;
import static com.github.leeonky.dal.runtime.ExpressionException.illegalOperation;
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

    @Deprecated
    public T instance() {
        return value();
    }

    public T value() {
        return value;
    }

    public Set<?> fieldNames() {
        return context.findPropertyReaderNames(this);
    }

    public boolean isList() {
        Object instance = value();
        return context.isRegisteredList(instance) || (instance != null && instance.getClass().isArray());
    }

    public DataList list() {
        if (list == null) {
            if (!isList())
                throw new DALRuntimeException(format("Invalid input value, expect a List but: %s", dump().trim()));
            list = new DataList(context.createCollection(value()));
        }
        return list;
    }

    public boolean isNull() {
        return context.isNull(value());
    }

    public Data<?> property(List<Object> propertyChain) {
        return propertyChain.isEmpty() ? this :
                property(propertyChain.get(0)).property(propertyChain.subList(1, propertyChain.size()));
    }

    public Data<?> property(Object propertyChain) {
        List<Object> chain = schemaType.access(propertyChain).getPropertyChainBefore(schemaType);
        if (chain.size() == 1 && chain.get(0).equals(propertyChain)) {
            try {
                return isList() && !(propertyChain instanceof String)
                        ? new Data<>(list().getByIndex((int) propertyChain), context, schemaType.access(propertyChain))
                        : context.accessProperty(this, propertyChain);
            } catch (IndexOutOfBoundsException ex) {
                throw new DALRuntimeException(ex.getMessage());
            } catch (ListMappingElementAccessException | ExpressionException | InterpreterException |
                     PropertyAccessException ex) {
                throw ex;
            } catch (Throwable e) {
                throw new PropertyAccessException(propertyChain, e);
            }
        }
        return property(chain);
    }

    public SchemaType propertySchema(Object property, boolean isListMapping) {
        return isListMapping ? schemaType.mappingAccess(property) : schemaType.access(property);
    }

    public Object firstFieldFromAlias(Object alias) {
        return schemaType.firstFieldFromAlias(alias);
    }

    public Data<?> tryConvert(Class<?>... targets) {
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

    public <N> Data<N> convert(Class<N> target) {
        return map(object -> context.getConverter().convert(target, object));
    }

    public <N> Data<N> map(Function<T, N> mapper) {
        return new Data<>(mapper.apply(value()), context, schemaType);
    }

    public Data<?> filter(String prefix) {
        FilteredObject filteredObject = new FilteredObject();
        fieldNames().stream().filter(String.class::isInstance).map(String.class::cast)
                .filter(field -> field.startsWith(prefix)).forEach(fieldName ->
                        filteredObject.put(fieldName.substring(prefix.length()), property(fieldName).value()));
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
        return BeanClass.cast(value(), type);
    }

    public boolean instanceOf(Class<?> type) {
        try {
            return type.isInstance(value());
        } catch (Throwable ignore) {
            return false;
        }
    }

    public Optional<CurryingMethod> currying(Object property) {
        return context.currying(value(), property);
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
                            .sorted(comparator).map(Data::value).collect(toList())) {
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
    }
}
