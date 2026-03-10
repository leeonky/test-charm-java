package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;

class SubValueBuilder extends SubBuilder {
    protected final Object value;

    public SubValueBuilder(String property, Object value, SubCollectionBuilder parentCollectionBuilder) {
        super(property, parentCollectionBuilder);
        this.value = value;
    }

    @Override
    protected SubBuilder mergeTo(SubBuilder to) {
        return to.mergeFrom(this);
    }

    @Override
    public Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory,
                                     BeanClass<?> collectionSpecElementType) {
        return new FixedValueProducer<>(parent.getType().getProperty(property()).getWriterType(),
                factory.transform(resolveNameForTransformer(), value));
    }
}
