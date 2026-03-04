package org.testcharm.jfactory;

import java.util.List;

abstract class SubBuilder {
    protected final String property;

    protected SubBuilder(String property) {
        this.property = property;
    }

    public String property() {
        return property;
    }

    public abstract Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory);

    protected abstract SubBuilder mergeTo(SubBuilder subBuilder);

    protected SubBuilder mergeFrom(SubValueBuilder subValueBuilder) {
        return this;
    }

    static SubBuilder create(List<KeyValue> keyValues) {
        return keyValues.stream().map(keyValue -> create(keyValue.key(), keyValue.getValue()))
                .reduce(SubBuilder::mergeTo).get();
    }

    private static SubBuilder create(String key, Object value) {
        return new SubValueBuilder(key, value);
    }
}
