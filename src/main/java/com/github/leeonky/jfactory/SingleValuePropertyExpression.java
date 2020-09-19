package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.Objects;

class SingleValuePropertyExpression<H, B> extends PropertyExpression<H, B> {
    private final Object value;
    private final String[] mixIns;
    private final String definition;

    public SingleValuePropertyExpression(Object value, String[] mixIns, String definition, String property,
                                         BeanClass<H> hostClass, BeanClass<B> beanClass) {
        super(property, hostClass, beanClass);
        this.value = value;
        this.mixIns = mixIns;
        this.definition = definition;
    }

    @Override
    protected <P> boolean isMatch(BeanClass<P> propertyType, P propertyValue) {
        return Objects.equals(propertyValue, hostClass.getConverter().tryConvert(propertyType.getType(), value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Producer<?> buildProducer(FactorySet factorySet, Instance<B> instance) {
        if (isIntently())
            return toBuilder(factorySet, hostClass.getPropertyWriter(property).getType().getElementOrPropertyType().getType()).createProducer(property);
        return new FixedValueProducer(hostClass.getPropertyWriter(property).getType(), value);
    }

    @Override
    public PropertyExpression<H, B> merge(PropertyExpression<H, B> propertyExpression) {
        return propertyExpression.mergeTo(this);
    }

    @Override
    protected PropertyExpression<H, B> mergeTo(SingleValuePropertyExpression<H, B> singleValuePropertyExpression) {
        return this;
    }

    private Builder<?> toBuilder(FactorySet factorySet, Class<?> propertyType) {
        return (definition != null ?
                factorySet.spec(definition)
                : factorySet.type(propertyType))
                .mixIn(mixIns);
    }
}
