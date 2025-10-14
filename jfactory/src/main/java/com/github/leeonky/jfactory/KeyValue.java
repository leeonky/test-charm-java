package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.GenericBeanClass;
import com.github.leeonky.util.Property;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.leeonky.util.Sneaky.cast;
import static java.lang.Integer.parseInt;

//TODO use a parser to parse this
class KeyValue {
    private static final String PATTERN_PROPERTY = "([^.(!\\[]+)";
    private static final String PATTERN_COLLECTION_INDEX = "(\\[(-?\\d+)])?";
    private static final String PATTERN_SPEC_TRAIT_WORD = "[^, )]";
    private static final String PATTERN_TRAIT = "((" + PATTERN_SPEC_TRAIT_WORD + "+[, ])(" + PATTERN_SPEC_TRAIT_WORD + "+[, ])*)?";
    private static final String PATTERN_SPEC = "(" + PATTERN_SPEC_TRAIT_WORD + "+)";
    private static final String PATTERN_TRAIT_SPEC = "(\\(" + PATTERN_TRAIT + PATTERN_SPEC + "\\))?";
    private static final String PATTERN_CLAUSE = "(\\." + "(.+)" + ")?";
    private static final String PATTERN_INTENTLY = "(!)?";
    private static final int GROUP_PROPERTY = 1;

    private static final int GROUP_TRAIT = 3;
    private static final int GROUP_SPEC = 6;

    private static final int GROUP_COLLECTION_INDEX = 3 + 5;
    private static final int GROUP_ELEMENT_TRAIT = 5 + 5;
    private static final int GROUP_ELEMENT_SPEC = 8 + 5;
    private static final int GROUP_INTENTLY = 9 + 5;
    private static final int GROUP_CLAUSE = 11 + 5;

    private final String key;
    private final Object value;
    private final Matcher matcher;
    private final String propertyName;
    private final String clause;

    public KeyValue(String key, Object value) {
        this.key = key;
        this.value = value;
        if (key != null) {
            matcher = Pattern.compile(PATTERN_PROPERTY + PATTERN_TRAIT_SPEC + PATTERN_COLLECTION_INDEX +
                    PATTERN_TRAIT_SPEC + PATTERN_INTENTLY + PATTERN_CLAUSE).matcher(key);
            if (!matcher.matches())
                throw new IllegalArgumentException(String.format("The format of property `%s` is invalid.", key));
            propertyName = matcher.group(GROUP_PROPERTY);
            clause = matcher.group(GROUP_CLAUSE);
        } else {
            matcher = null;
            propertyName = null;
            clause = null;
        }
    }

    public <T> Expression<T> createExpression(BeanClass<T> beanClass, ObjectFactory<T> objectFactory, Producer<T> producer, boolean forQuery) {
        Property<T> property = beanClass.getProperty(propertyName);
        TraitsSpec traitsSpec = new TraitsSpec(matcher.group(GROUP_TRAIT) != null ?
                matcher.group(GROUP_TRAIT).split(", |,| ") : new String[0], matcher.group(GROUP_SPEC));
        Producer<?> subProducer;
        if (traitsSpec.isCollectionSpec() && producer instanceof ObjectProducer &&
                (property.getWriterType().is(Object.class) || property.getWriterType().isCollection())) {
            SpecClassFactory<T> specFactory = objectFactory.getFactorySet().querySpecClassFactory(traitsSpec.spec());
            property = property.decorateType(GenericBeanClass.create(
                    property.getWriterType().isCollection() ? property.getWriterType().getType() :
                            List.class, specFactory.getType().getGenericType()));
            subProducer = ((ObjectProducer) producer).forceChildOrDefaultCollection(property.getWriter());
            if (subProducer instanceof CollectionProducer)
                ((CollectionProducer<?, ?>) subProducer).changeElementPopulationFactory(i ->
                        traitsSpec.toBuilder(((ObjectProducer) producer).jFactory(), specFactory.getType()).createProducer());
            if (subProducer == null)
                subProducer = PlaceHolderProducer.PLACE_HOLDER;
        } else {
            subProducer = producer.getChild(propertyName).orElse(PlaceHolderProducer.PLACE_HOLDER);
            if (subProducer instanceof ObjectProducer ||
                    subProducer instanceof CollectionProducer && property.getWriterType().is(Object.class)) {
                property = property.decorateType(subProducer.getType());
            }
//       for optional spec: type is List,  optional spec in spec
            if (subProducer instanceof CollectionProducer && property.getWriterType().isCollection()
                    && property.getWriterType().getElementType().is(Object.class)) {
                property = property.decorateType(subProducer.getType());
            }
        }
        Property<T> finalSubProperty = property;
        Producer<T> finalSubProducer = (Producer<T>) subProducer;
        return hasIndex().map(index -> createCollectionExpression(finalSubProperty, index, objectFactory, finalSubProducer, forQuery))
                .orElseGet(() -> createSubExpression(finalSubProperty, null, objectFactory, finalSubProducer, forQuery, traitsSpec));
    }

    private <T> Expression<T> createCollectionExpression(Property<T> property, String index,
                                                         ObjectFactory<T> objectFactory, Producer<?> collectionProducer, boolean forQuery) {
        Property<?> propertySub = property.getWriter().getType().getProperty(index);
        int intIndex = parseInt(index);
        Producer<?> subProducer;
        if (collectionProducer instanceof CollectionProducer) {
            subProducer = ((CollectionProducer<?, ?>) collectionProducer).newElementPopulationProducer(cast(collectionProducer.getType().getPropertyWriter(index)));
            if (subProducer instanceof ObjectProducer) {
                propertySub = propertySub.decorateType(subProducer.getType());
            }
        } else
            subProducer = collectionProducer.getChild(index).orElse(PlaceHolderProducer.PLACE_HOLDER);
        TraitsSpec elementTraitSpec = new TraitsSpec(matcher.group(GROUP_ELEMENT_TRAIT) != null ?
                matcher.group(GROUP_ELEMENT_TRAIT).split(", |,| ") : new String[0], matcher.group(GROUP_ELEMENT_SPEC));
        return new CollectionExpression<>(property, intIndex,
                createSubExpression(propertySub, property, objectFactory, subProducer, forQuery, elementTraitSpec), forQuery);
    }

    private Optional<String> hasIndex() {
        return Optional.ofNullable(matcher.group(GROUP_COLLECTION_INDEX));
    }


    private boolean isEmptyMap(Object value) {
        return value instanceof Map && ((Map<?, ?>) value).isEmpty();
    }

    private <T> Expression<T> createSubExpression(Property<T> property, Property<?> parentProperty,
                                                  ObjectFactory<?> objectFactory, Producer<?> subProducer, boolean forQuery, TraitsSpec traitsSpec) {
        Expression<T> result;
        Property<T> decoratedProperty = traitsSpec.guessPropertyType(objectFactory).map(property::decorateType).orElse(property);
        if (clause == null) {
            String transformerName = parentProperty != null && decoratedProperty.getBeanType().isCollection()
                    ? propertyName + "[]" : propertyName;
            Object value = objectFactory.transform(transformerName, this.value);
            if (isEmptyMap(value))
                result = new SubObjectExpression<>(new KeyValueCollection(), traitsSpec, decoratedProperty, objectFactory, subProducer, forQuery);
            else
                result = new SingleValueExpression<>(value, traitsSpec, decoratedProperty, forQuery);
        } else
            result = new SubObjectExpression<>(new KeyValueCollection().append(clause, value), traitsSpec, decoratedProperty, objectFactory, subProducer, forQuery);
        return result.setIntently(matcher.group(GROUP_INTENTLY) != null);
    }

    public <T> Builder<T> apply(Builder<T> builder) {
        return builder.property(key, value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(KeyValue.class, key, value);
    }

    @Override
    public boolean equals(Object another) {
        return BeanClass.cast(another, KeyValue.class)
                .map(keyValue -> Objects.equals(key, keyValue.key) && Objects.equals(value, keyValue.value))
                .orElseGet(() -> super.equals(another));
    }

    public Object getValue() {
        return value;
    }
}

