package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;
import com.github.leeonky.util.ThrowingSupplier;

import java.lang.RuntimeException;
import java.util.*;
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
    private RuntimeException error;
    private Function<PropertyAccessException, RuntimeException> errorMapper = e -> e;

    public Data(Object instance, DALRuntimeContext context, SchemaType schemaType) {
        this(() -> instance, context, schemaType);
    }

    public Data(ThrowingSupplier<?> supplier, DALRuntimeContext context, SchemaType schemaType) {
        this.supplier = supplier;
        this.context = context;
        this.schemaType = schemaType;
    }

    public Data mapError(Function<PropertyAccessException, RuntimeException> mapper) {
        errorMapper = mapper;
        return this;
    }

    public Object instance() {
        if (error != null)
            throw error;
        if (resolved)
            return instance;
        try {
            resolved = true;
            return instance = supplier.get();
        } catch (PropertyAccessException e) {
            throw error = errorMapper.apply(e);
        } catch (UserRuntimeException e) {
            throw error = errorMapper.apply(new PropertyAccessException(e.getCause().getMessage(), e));
        } catch (Throwable e) {
            throw error = errorMapper.apply(new PropertyAccessException(e.getMessage(), e));
        }
    }

    public Set<?> fieldNames() {
        return context.findPropertyReaderNames(instance());
    }

    public boolean isList() {
        return context.isRegisteredList(instance()) || (instance() != null && instance().getClass().isArray());
    }

    public DataList list() {
        if (list == null) {
            if (!isList())
                throw new java.lang.RuntimeException(format("Invalid input value, expect a List but: %s", dumpAll().trim()));
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
                    return getPropertyValue(propertyChain);
                } catch (IndexOutOfBoundsException ex) {
                    throw new PropertyAccessException(ex.getMessage(), ex);
                } catch (ListMappingElementAccessException ex) {
                    throw new PropertyAccessException(ex.toDalError(0).getMessage(),
                            ex.propertyAccessException().getCause());
                } catch (UserRuntimeException e) {
                    throw buildPropertyAccessException(propertyChain, e, e.getCause().toString());
                } catch (Exception e) {
                    throw buildPropertyAccessException(propertyChain, e, e.getMessage());
                }
            };
            return new Data(supplier, context, propertySchema(propertyChain));
        }
        return getValue(chain);
    }

    private PropertyAccessException buildPropertyAccessException(Object propertyChain, Throwable cause, String message) {
        return new PropertyAccessException(format("Get property `%s` failed, property can be:\n" +
                        "  1. public field\n" +
                        "  2. public getter\n" +
                        "  3. public method\n" +
                        "  4. Map key value\n" +
                        "  5. customized type getter\n" +
                        "  6. static method extension\n%s%s",
                propertyChain, message, listMappingMessage(this, propertyChain)), cause);
    }

    private String listMappingMessage(Data data, Object symbol) {
        return data.isList() ? format("\nImplicit list mapping is not allowed in current version of DAL, use `%s[]` instead or specify index in []",
                symbol) : "";
    }

    private Object getPropertyValue(Object property) {
        return isList() ? fetchFromList(property) : context.getPropertyValue(this, property);
    }

    private Object fetchFromList(Object property) {
        return property instanceof String ? context.getPropertyValue(this, property) :
                list().getByIndex((int) property);
    }

    private SchemaType propertySchema(Object property) {
        if (isList() && property instanceof String)
            return schemaType.mappingAccess(property);
        return schemaType.access(property);
    }

    public Object firstFieldFromAlias(Object alias) {
        return schemaType.firstFieldFromAlias(alias);
    }

    public Data convert(Class<?> target) {
        return new Data(context.getConverter().convert(target, instance()), context, schemaType);
    }

    public Data map(Function<Object, Object> mapper) {
        return new Data(mapper.apply(instance()), context, schemaType);
    }

    public Data filter(String prefix) {
        FilteredObject filteredObject = new FilteredObject();
        fieldNames().stream().filter(String.class::isInstance).map(String.class::cast)
                .filter(field -> field.startsWith(prefix)).forEach(fieldName ->
                        filteredObject.put(fieldName.substring(prefix.length()), getValue(fieldName).instance()));
        return new Data(filteredObject, context, schemaType);
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

    public boolean instanceOf(Class<?> type) {
        try {
            return type.isInstance(instance());
        } catch (Exception e) {
            return false;
        }
    }

    static class FilteredObject extends LinkedHashMap<String, Object> implements PartialObject {
    }

    public class DataList extends DALCollection.Decorated<Object> {
        public DataList(DALCollection<Object> origin) {
            super(origin);
        }

        public DALCollection<Data> wraps() {
            return map((index, e) -> new Data(e, context, schemaType.access(index)));
        }

        public Data listMap(Object property) {
            return new Data(listMap(data -> data.getValue(property).instance()), context, propertySchema(property));
        }

        public AutoMappingList listMap(Function<Data, Object> mapper) {
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

        public Data wrap() {
            return new Data(this, context, schemaType);
        }
    }
}
