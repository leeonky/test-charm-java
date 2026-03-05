package org.testcharm.jfactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class SubBuilder {
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("[^.(!\\[]+");

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

    static SubBuilder create(String key, Object value) {
        key = key.trim();
        Matcher matcher = PROPERTY_PATTERN.matcher(key);
        if (matcher.lookingAt()) {
            String property = matcher.group(0);
            String remaining = key.substring(matcher.end());
            if (remaining.isEmpty() && !isEmptyMap(value)) {
                return new SubValueBuilder(property, value);
            }
        }
        throw new IllegalArgumentException("Illegal property format: " + key);
    }


    private static boolean isEmptyMap(Object value) {
        return value instanceof Map && ((Map<?, ?>) value).isEmpty();
    }
}
