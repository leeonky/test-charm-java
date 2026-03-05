package org.testcharm.jfactory;

import java.util.Map;
import java.util.Optional;
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

    public abstract Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory);

    protected abstract SubBuilder mergeTo(SubBuilder subBuilder);

    protected SubBuilder mergeFrom(SubValueBuilder subValueBuilder) {
        return this;
    }

    protected SubBuilder mergeFrom(SubObjectBuilder subValueBuilder) {
        return this;
    }

    static SubBuilder create(String key, Object value) {
        return new BuilderParser(key).parse(value);
    }

    private static boolean isEmptyMap(Object value) {
        return value instanceof Map && ((Map<?, ?>) value).isEmpty();
    }
}

class BuilderParser extends Parser {
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("[^.(!\\[]+");
    private static final Pattern FORCE_PATTERN = Pattern.compile("!");

    public BuilderParser(String content) {
        super(content);
    }

    public SubBuilder parse(Object value) {
        return pop(PROPERTY_PATTERN).map(property -> {
            if (isEmpty())
                return isEmptyMap(value) ? new SubObjectBuilder(property, false) : new SubValueBuilder(property, value);
            else {
                return pop(FORCE_PATTERN).map(force -> {
                    if (isEmpty())
                        return new SubObjectBuilder(property, true);
                    else {
                        throw new IllegalArgumentException("Illegal property format: " + content());
                    }
                }).orElseThrow(() ->
                        new IllegalArgumentException("Illegal property format: " + content())
                );
            }

        }).orElseThrow(() -> new IllegalArgumentException("Illegal property format: " + content()));
    }

    private static boolean isEmptyMap(Object value) {
        return value instanceof Map && ((Map<?, ?>) value).isEmpty();
    }
}

class Parser {
    private String content;

    public Parser(String content) {
        this.content = content.trim();
    }

    public Optional<String> pop(Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.lookingAt()) {
            content = content.substring(matcher.end()).trim();
            return Optional.of(matcher.group(0));
        }
        return Optional.empty();
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public String content() {
        return content;
    }
}
