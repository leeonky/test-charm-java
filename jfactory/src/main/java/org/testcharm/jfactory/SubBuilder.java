package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class SubBuilder {
    private final String property;
    private final SubCollectionBuilder parentCollectionBuilder;

    protected SubBuilder(String property, SubCollectionBuilder parentCollectionBuilder) {
        this.property = property;
        this.parentCollectionBuilder = parentCollectionBuilder;
    }

    public String property() {
        return property;
    }

    public SubCollectionBuilder parentCollectionBuilder() {
        return parentCollectionBuilder;
    }

    public abstract Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory,
                                              BeanClass<?> collectionSpecElementType);

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

    static SubBuilder create(String key, Object value, SubCollectionBuilder parentCollectionBuilder) {
        return new BuilderParser(key).parse(value, parentCollectionBuilder);
    }

    protected String resolveNameForTransformer() {
        return parentCollectionBuilder() != null ? parentCollectionBuilder().property() + "[]" : property();
    }
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

    public SubBuilder parse(Object value, SubCollectionBuilder parentCollectionBuilder) {
        return pop(PROPERTY_PATTERN).map(property -> createSubBuilder(property, value, parentCollectionBuilder))
                .orElseGet(() -> pop1(INDEX_PATTERN).map(property -> createSubBuilder(property, value, parentCollectionBuilder))
                        .orElseThrow(() -> new IllegalArgumentException("Illegal property format: " + content())));
    }

    private SubBuilder createSubBuilder(String property, Object value, SubCollectionBuilder parentCollectionBuilder) {
        if (isEmpty()) {
            if (isEmptyMap(value))
                return new SubObjectBuilder(property, new TraitsSpec(), false, parentCollectionBuilder);
            return new SubValueBuilder(property, value, parentCollectionBuilder);
        } else {
            return pop1(PATTERN_TRAIT_SPEC).map(TraitsSpec::new).map(traitsSpec -> {
                if (isEmpty())
                    return new SubObjectBuilder(property, traitsSpec, false, parentCollectionBuilder);
                return checkForceAndCreateSubBuilder(value, property, traitsSpec, parentCollectionBuilder);
            }).orElseGet(() -> checkForceAndCreateSubBuilder(value, property, new TraitsSpec(), parentCollectionBuilder));
        }
    }

    private SubBuilder checkForceAndCreateSubBuilder(Object value, String property, TraitsSpec traitsSpec, SubCollectionBuilder parentCollectionBuilder) {
        return pop(FORCE_PATTERN).map(force -> {
            if (isEmpty())
                return new SubObjectBuilder(property, traitsSpec, true, parentCollectionBuilder);
            return createSubObjectBuilder(property, traitsSpec, true, value, parentCollectionBuilder);
        }).orElseGet(() -> createSubObjectBuilder(property, traitsSpec, false, value, parentCollectionBuilder));
    }

    private SubBuilder createSubObjectBuilder(String property, TraitsSpec traitsSpec, boolean force, Object value, SubCollectionBuilder parentCollectionBuilder) {
        String clause = content();
        if (clause.startsWith("."))
            return new SubObjectBuilder(property, traitsSpec, force, clause.substring(1), value, parentCollectionBuilder);
        if (clause.startsWith("["))
            return new SubCollectionBuilder(property, traitsSpec, force, clause, value, parentCollectionBuilder);
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
