package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;

import static java.lang.String.format;
import static org.testcharm.jfactory.JFactory.beanClass;

public interface DefaultValueFactory<V> {
    <T> V create(BeanClass<T> beanType, ObjectProperty<T> objectProperty);

    @SuppressWarnings("unchecked")
    default Class<V> getType() {
        return (Class<V>) beanClass(getClass()).getSuper(DefaultValueFactory.class).getTypeArguments(0)
                .orElseThrow(() -> new IllegalStateException(format("Cannot guess type `%s` via generic type argument,"
                        + " please override DefaultValueFactory::getType", getClass().getName())))
                .getType();
    }
}
