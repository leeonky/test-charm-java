package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;
import org.testcharm.util.PropertyReader;

import java.util.Objects;

class ValueNode extends PropertyNode {
    protected final Object value;

    public ValueNode(String property, Object value) {
        super(property);
        this.value = value;
    }

    @Override
    protected PropertyNode mergeTo(PropertyNode to) {
        return to.mergeFrom(this);
    }

    @Override
    public Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory) {
        return new FixedValueProducer<>(parent.getType().getProperty(property()).getWriterType(), value);
    }

    @Override
    public boolean matches(Object object, ObjectFactory<?> objectFactory) {
        if (object != null) {
            PropertyReader<Object> propertyReader = BeanClass.createFrom(object).getPropertyReader(property());
            return Objects.equals(propertyReader.tryConvert(value), propertyReader.getValue(object));
        }
        return false;
    }


}
