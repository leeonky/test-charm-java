package org.testcharm.jfactory;

import org.testcharm.util.TextCursor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

abstract class PropertyNode {
    private final String property;

    protected PropertyNode(String property) {
        this.property = property;
    }

    public static List<PropertyNode> groupByProperty(List<PropertyNode> propertyNodes) {
        return propertyNodes.stream().collect(Collectors.groupingBy(PropertyNode::property, LinkedHashMap::new, Collectors.toList())).values().stream()
                .map(grouped -> grouped.stream().reduce(PropertyNode::mergeTo))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    public String property() {
        return property;
    }

    public abstract Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory);

    protected abstract PropertyNode mergeTo(PropertyNode to);

    protected PropertyNode mergeFrom(ValueNode from) {
        return this;
    }

    protected PropertyNode mergeFrom(ObjectNode from) {
        return this;
    }

    protected PropertyNode mergeFrom(CollectionNode from) {
        return this;
    }

    static PropertyNode create(String key, Object value, CollectionNode parentCollectionBuilder, ObjectFactory<?> objectFactory) {
        return new BuilderParser(key, parentCollectionBuilder, objectFactory).parse(value);
    }

    public PropertyNode forceCreate() {
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
    private final CollectionNode parent;
    private final ObjectFactory<?> objectFactory;

    public BuilderParser(String content, CollectionNode parent, ObjectFactory<?> objectFactory) {
        super(content);
        this.parent = parent;
        this.objectFactory = objectFactory;
    }

    public PropertyNode parse(Object value) {
        return pop(PROPERTY_PATTERN).map(property -> createPropertyNode(property, value))
                .orElseGet(() -> pop1(INDEX_PATTERN).map(property -> createPropertyNode(property, value))
                        .orElseThrow(() -> new IllegalArgumentException(String.format("The format of property `%s` is invalid.", content()))));
    }

    private PropertyNode createPropertyNode(String property, Object value) {
        if (isEmpty()) {
            if (isEmptyMap(value))
                return new ObjectNode(property, new TraitsSpec(), false);
            return new ValueNode(property, transform(property, value));
        } else {
            return pop1(PATTERN_TRAIT_SPEC).map(TraitsSpec::new).map(traitsSpec -> {
                if (isEmpty())
                    return new ObjectNode(property, traitsSpec, false);
                return checkForceAndCreateNode(value, property, traitsSpec);
            }).orElseGet(() -> checkForceAndCreateNode(value, property, new TraitsSpec()));
        }
    }

    private Object transform(String property, Object value) {
        String transformerName = parent != null ? parent.property() + "[]" : property;
        return objectFactory.transform(transformerName, value);
    }

    private PropertyNode checkForceAndCreateNode(Object value, String property, TraitsSpec traitsSpec) {
        return pop(FORCE_PATTERN).map(force -> {
            if (isEmpty())
                return new ObjectNode(property, traitsSpec, true);
            return createObjectNode(property, traitsSpec, true, value);
        }).orElseGet(() -> createObjectNode(property, traitsSpec, false, value));
    }

    private PropertyNode createObjectNode(String property, TraitsSpec traitsSpec, boolean force, Object value) {
        String clause = leftContent();
        if (clause.startsWith("."))
            return new ObjectNode(property, traitsSpec, force).append(clause.substring(1), value);
        else if (clause.startsWith("["))
            return new CollectionNode(property, traitsSpec, force, parent).append(clause, value);
        throw new IllegalArgumentException(String.format("The format of property `%s` is invalid.", content()));
    }

    private static boolean isEmptyMap(Object value) {
        return value instanceof Map && ((Map<?, ?>) value).isEmpty();
    }
}
