package org.testcharm.jfactory;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class SubBuilder {
    protected final String property;

    protected SubBuilder(String property) {
        this.property = property;
    }

    public String property() {
        return property;
    }

    public abstract Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory);

    protected abstract SubBuilder mergeTo(SubBuilder to);

    protected SubBuilder mergeFrom(SubValueBuilder from) {
        return this;
    }

    protected SubBuilder mergeFrom(SubObjectBuilder from) {
        return this;
    }

    static SubBuilder create(String key, Object value) {
        return new BuilderParser(key).parse(value);
    }
}

class BuilderParser extends Parser {
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("[^.(!\\[]+");
    private static final Pattern FORCE_PATTERN = Pattern.compile("!");
    private static final String PATTERN_SPEC_TRAIT_WORD = "[^,\\s()]+";
    private static final Pattern PATTERN_TRAIT_SPEC = Pattern.compile("\\((" + PATTERN_SPEC_TRAIT_WORD + "(?:[\\s,]+"
            + PATTERN_SPEC_TRAIT_WORD + ")*)\\)");

    public BuilderParser(String content) {
        super(content);
    }

    public SubBuilder parse(Object value) {
        return pop(PROPERTY_PATTERN).map(property -> {
            if (isEmpty()) {
                if (isEmptyMap(value))
                    return new SubObjectBuilder(property);
                return new SubValueBuilder(property, value);
            } else {
                return pop1(PATTERN_TRAIT_SPEC).map(TraitsSpec::new).map(traitsSpec -> {
                    if (isEmpty())
                        return new SubObjectBuilder(property, traitsSpec);
                    return pop(FORCE_PATTERN).map(force -> {
                        if (isEmpty())
                            return new SubObjectBuilder(property, traitsSpec, true);
                        throw new IllegalArgumentException("Illegal property format: " + content());
                    }).orElseGet(() -> {
                        String clause = content();
                        if (clause.startsWith("."))
                            return new SubObjectBuilder(property, traitsSpec, clause.substring(1), value);
                        throw new IllegalArgumentException("Illegal property format: " + content());
                    });
                }).orElseGet(() -> pop(FORCE_PATTERN).map(force -> {
                    if (isEmpty())
                        return new SubObjectBuilder(property, true);
                    throw new IllegalArgumentException("Illegal property format: " + content());
                }).orElseGet(() -> {
                    String clause = content();
                    if (clause.startsWith("."))
                        return new SubObjectBuilder(property, clause.substring(1), value);
                    throw new IllegalArgumentException("Illegal property format: " + content());
                }));
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

    public Optional<String[]> popGroup(Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.lookingAt()) {
            content = content.substring(matcher.end()).trim();
            String[] items = new String[matcher.groupCount() + 1];
            for (int i = 0; i <= matcher.groupCount(); i++)
                items[i] = matcher.group(i);
            return Optional.of(items);
        }
        return Optional.empty();
    }

    public Optional<String> pop(Pattern pattern) {
        return popGroup(pattern).map(items -> items[0]);
    }

    public Optional<String> pop1(Pattern pattern) {
        return popGroup(pattern).map(items -> items[1]);
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public String content() {
        return content;
    }
}
