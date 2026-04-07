package org.testcharm.dal.runtime;

import org.testcharm.util.BeanClass;
import org.testcharm.util.Sneaky;

import java.util.Objects;
import java.util.Set;

import static org.testcharm.dal.runtime.DALException.buildUserRuntimeException;

public interface PropertyAccessor<T> {

    default Data<?> getData(Data<T> data, Object property) {
        Object result = null;
        try {
            result = getValue(data, property);
        } catch (InvalidPropertyException e) {
            try {
                result = data.currying(property).orElseThrow(() -> e).resolve();
            } catch (Throwable e1) {
                Sneaky.sneakyThrow(buildUserRuntimeException(e1));
            }
        } catch (Throwable e) {
            Sneaky.sneakyThrow(buildUserRuntimeException(e));
        }
        SchemaType schemaType = data.propertySchema(property, data.instanceOf(AutoMappingList.class) && property instanceof String);
        Object finalResult = result;
        return data.map(ig -> finalResult, schemaType);
    }

    default Object getValue(Data<T> data, Object property) {
        return getValue(data.value(), property);
    }

    default Object getValue(T instance, Object property) {
        return BeanClass.createFrom(instance).getPropertyValue(instance, String.valueOf(property));
    }

    default Set<?> getPropertyNames(Data<T> data) {
        return getPropertyNames(data.value());
    }

    default Set<?> getPropertyNames(T instance) {
        return BeanClass.createFrom(instance).getPropertyReaders().keySet();
    }

    default boolean isNull(T instance) {
        return Objects.equals(instance, null);
    }
}
