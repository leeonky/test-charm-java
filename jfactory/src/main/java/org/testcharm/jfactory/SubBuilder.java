package org.testcharm.jfactory;

import org.testcharm.util.TextCursor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

abstract class SubBuilder {
    private final String property;

    protected SubBuilder(String property) {
        this.property = property;
    }

    public static List<SubBuilder> groupByProperty(List<SubBuilder> builders) {
        return builders.stream().collect(Collectors.groupingBy(SubBuilder::property, LinkedHashMap::new, Collectors.toList())).values().stream()
                .map(subBuilders -> subBuilders.stream().reduce(SubBuilder::mergeTo))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
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

    protected SubBuilder mergeFrom(SubCollectionBuilder from) {
        return this;
    }

    static SubBuilder create(String key, Object value, SubCollectionBuilder parentCollectionBuilder, ObjectFactory<?> objectFactory) {
        return new BuilderParser(key, parentCollectionBuilder, objectFactory).parse(value);
    }

    public SubBuilder forceCreate() {
        return this;
    }

    public abstract boolean matches(Object object, ObjectFactory<?> objectFactory);
}

class BuilderParser extends TextCursor {
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("[^.(!\\[]+");
    private static final Pattern FORCE_PATTERN = Pattern.compile("!");
    private static final String PATTERN_SPEC_TRAIT_WORD = "[^,\\s()]+";
    private static final Pattern PATTERN_TRAIT_SPEC = Pattern.compile("\\((" + PATTERN_SPEC_TRAIT_WORD + "(?:[\\s,]+"
            + PATTERN_SPEC_TRAIT_WORD + ")*)\\)");
    private static final Pattern INDEX_PATTERN = Pattern.compile("\\[(-?\\d+)\\]");
    private final SubCollectionBuilder parent;
    private final ObjectFactory<?> objectFactory;

    public BuilderParser(String content, SubCollectionBuilder parent, ObjectFactory<?> objectFactory) {
        super(content);
        this.parent = parent;
        this.objectFactory = objectFactory;
    }

    public SubBuilder parse(Object value) {
        return pop(PROPERTY_PATTERN).map(property -> createSubBuilder(property, value))
                .orElseGet(() -> pop1(INDEX_PATTERN).map(property -> createSubBuilder(property, value))
                        .orElseThrow(() -> new IllegalArgumentException(String.format("The format of property `%s` is invalid.", content()))));
    }

    private SubBuilder createSubBuilder(String property, Object value) {
        if (isEmpty()) {
            if (isEmptyMap(value))
                return new SubObjectBuilder(property, new TraitsSpec(), false);
            return new SubValueBuilder(property, transform(property, value));
        } else {
            return pop1(PATTERN_TRAIT_SPEC).map(TraitsSpec::new).map(traitsSpec -> {
                if (isEmpty())
                    return new SubObjectBuilder(property, traitsSpec, false);
                return checkForceAndCreateSubBuilder(value, property, traitsSpec);
            }).orElseGet(() -> checkForceAndCreateSubBuilder(value, property, new TraitsSpec()));
        }
    }

    private Object transform(String property, Object value) {
        String transformerName = parent != null ? parent.property() + "[]" : property;
        return objectFactory.transform(transformerName, value);
    }

    private SubBuilder checkForceAndCreateSubBuilder(Object value, String property, TraitsSpec traitsSpec) {
        return pop(FORCE_PATTERN).map(force -> {
            if (isEmpty())
                return new SubObjectBuilder(property, traitsSpec, true);
            return createSubObjectBuilder(property, traitsSpec, true, value);
        }).orElseGet(() -> createSubObjectBuilder(property, traitsSpec, false, value));
    }

    private SubBuilder createSubObjectBuilder(String property, TraitsSpec traitsSpec, boolean force, Object value) {
        String clause = leftContent();
        if (clause.startsWith("."))
            return new SubObjectBuilder(property, traitsSpec, force).append(clause.substring(1), value);
        else if (clause.startsWith("["))
            return new SubCollectionBuilder(property, traitsSpec, force, parent).append(clause, value);
        throw new IllegalArgumentException(String.format("The format of property `%s` is invalid.", content()));
    }

    private static boolean isEmptyMap(Object value) {
        return value instanceof Map && ((Map<?, ?>) value).isEmpty();
    }
}
