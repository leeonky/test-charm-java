package org.testcharm.util;

abstract class AbstractPropertyAccessor<T> implements PropertyAccessor<T> {
    private final BeanClass<T> beanClass;

    AbstractPropertyAccessor(BeanClass<T> beanClass) {
        this.beanClass = beanClass;
    }

    static String propertyNameInChain(PropertyAccessor<?> accessor) {
        return accessor.getBeanType().isCollection() ? "[" + accessor.getName() + "]" : "." + accessor.getName();
    }

    @Override
    public Object tryConvert(Object value) {
        return BeanClass.getConverter().tryConvert(getType().getType(), value);
    }

    @Override
    public BeanClass<T> getBeanType() {
        return beanClass;
    }

    @Override
    public BeanClass<?> getType() {
        return BeanClass.create(GenericType.createGenericType(getGenericType()));
    }
}
