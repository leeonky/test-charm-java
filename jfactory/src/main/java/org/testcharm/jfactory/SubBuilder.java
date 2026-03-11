package org.testcharm.jfactory;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class SubBuilder {
    private final String property;
    private final SubCollectionBuilder parentCollectionBuilder;

    protected SubBuilder(String property) {
        this.property = property;
        parentCollectionBuilder = null;
    }

    public String property() {
        return property;
    }

    public SubCollectionBuilder parentCollectionBuilder() {
        return parentCollectionBuilder;
    }

    public abstract Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory);

    protected abstract SubBuilder mergeTo(SubBuilder to);

    protected SubBuilder mergeFrom(SubValueBuilder from) {
        return this;
    }

    protected SubBuilder mergeFrom(SubObjectBuilder from) {
        return this;
    }

    protected SubBuilder mergeFrom(SubCollectionBuilder from) {
        return this;
    }

    static SubBuilder create(String key, Object value, SubCollectionBuilder parentCollectionBuilder, boolean queryFirst, ObjectFactory<?> objectFactory) {
        return new BuilderParser(key).parse(value, parentCollectionBuilder, queryFirst, objectFactory);
    }

    public SubBuilder forceCreate() {
        return this;
    }

    public abstract boolean matches(Object object, ObjectFactory<?> objectFactory);
}

class BuilderParser extends Parser {
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("[^.(!\\[]+");
    private static final Pattern FORCE_PATTERN = Pattern.compile("!");
    private static final String PATTERN_SPEC_TRAIT_WORD = "[^,\\s()]+";
    private static final Pattern PATTERN_TRAIT_SPEC = Pattern.compile("\\((" + PATTERN_SPEC_TRAIT_WORD + "(?:[\\s,]+"
            + PATTERN_SPEC_TRAIT_WORD + ")*)\\)");
    private static final Pattern INDEX_PATTERN = Pattern.compile("\\[(-?\\d+)\\]");

    public BuilderParser(String content) {
        super(content);
    }

    public SubBuilder parse(Object value, SubCollectionBuilder parentCollectionBuilder, boolean queryFirst, ObjectFactory<?> objectFactory) {
        return pop(PROPERTY_PATTERN).map(property -> createSubBuilder(property, value, parentCollectionBuilder, queryFirst, objectFactory))
                .orElseGet(() -> pop1(INDEX_PATTERN).map(property -> createSubBuilder(property, value, parentCollectionBuilder, queryFirst, objectFactory))
                        .orElseThrow(() -> new IllegalArgumentException("Illegal property format: " + content())));
    }

    private SubBuilder createSubBuilder(String property, Object value, SubCollectionBuilder parentCollectionBuilder, boolean queryFirst, ObjectFactory<?> objectFactory) {
        if (isEmpty()) {
            if (isEmptyMap(value))
                return new SubObjectBuilder(property, new TraitsSpec(), false, queryFirst);
            String transformerName = parentCollectionBuilder != null ? parentCollectionBuilder.property() + "[]" : property;
            return new SubValueBuilder(property, objectFactory.transform(transformerName, value));
        } else {
            return pop1(PATTERN_TRAIT_SPEC).map(TraitsSpec::new).map(traitsSpec -> {
                if (isEmpty())
                    return new SubObjectBuilder(property, traitsSpec, false, queryFirst);
                return checkForceAndCreateSubBuilder(value, property, traitsSpec, parentCollectionBuilder, queryFirst);
            }).orElseGet(() -> checkForceAndCreateSubBuilder(value, property, new TraitsSpec(), parentCollectionBuilder, queryFirst));
        }
    }

    private SubBuilder checkForceAndCreateSubBuilder(Object value, String property, TraitsSpec traitsSpec, SubCollectionBuilder parentCollectionBuilder, boolean queryFirst) {
        return pop(FORCE_PATTERN).map(force -> {
            if (isEmpty())
                return new SubObjectBuilder(property, traitsSpec, true, queryFirst);
            return createSubObjectBuilder(property, traitsSpec, true, value, parentCollectionBuilder, queryFirst);
        }).orElseGet(() -> createSubObjectBuilder(property, traitsSpec, false, value, parentCollectionBuilder, queryFirst));
    }

    private SubBuilder createSubObjectBuilder(String property, TraitsSpec traitsSpec, boolean force, Object value, SubCollectionBuilder parentCollectionBuilder, boolean queryFirst) {
        String clause = content();
        if (clause.startsWith("."))
            return new SubObjectBuilder(property, traitsSpec, force, clause.substring(1), value, queryFirst);
        if (clause.startsWith("["))
            return new SubCollectionBuilder(property, traitsSpec, force, clause, value, parentCollectionBuilder, queryFirst);
        throw new IllegalArgumentException("Illegal property format: " + content());
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
