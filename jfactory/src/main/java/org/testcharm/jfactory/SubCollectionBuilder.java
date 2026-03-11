package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;
import org.testcharm.util.PropertyWriter;

import java.util.List;
import java.util.stream.Collectors;

import static org.testcharm.util.CollectionHelper.reify;

class SubCollectionBuilder extends SubNestedBuilder {
    private final SubCollectionBuilder parent;

    public SubCollectionBuilder(String property, TraitsSpec traitsSpec, boolean force, String clause, Object value, SubCollectionBuilder parent, boolean queryFirst) {
        this(property, traitsSpec, force, parent, queryFirst);
        subProperties.put(clause, value);
    }

    public SubCollectionBuilder(String property, TraitsSpec traitsSpec, boolean force, SubCollectionBuilder parent, boolean queryFirst) {
        super(property, queryFirst, force, traitsSpec);
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    // TODO missing test [-1] [3] should process positive index first?  but [0] [-2] also incorrect
    public Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory) {
        if (traitsSpec.spec() != null) {
            return traitsSpec.isCollectionElementSpec() ?
                    processElementBuilder(jFactory, factory, generateCollectionProducerByInputSpec(parent, factory, jFactory))
                    : new BuilderValueProducer<>(traitsSpec.toBuilder(jFactory, null), false).apply(subProperties);
        } else {
            Producer<?> producer = parent.getChildOrDefaultCollection((PropertyWriter) parent.getType().getPropertyWriter(property()));
            return producer instanceof CollectionProducer
                    ? processElementBuilder(jFactory, factory, (CollectionProducer<?, ?>) producer)
                    : ((BuilderValueProducer<?>) producer).apply(subProperties);
        }
    }

    private CollectionProducer<?, ?> generateCollectionProducerByInputSpec(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory) {
        PropertyWriter<?> propertyWriter = parent.getType().getPropertyWriter(property());
        propertyWriter = propertyWriter.decorateType(reify(propertyWriter.getType().isCollection()
                ? propertyWriter.getType().getType()
                : List.class, traitsSpec.guessPropertyType(factory.factorySet()).get().getGenericType()));
        CollectionProducer<?, ?> collectionProducer = ((ObjectProducer<?>) parent).createCollectionProducer((PropertyWriter) propertyWriter);
        collectionProducer.changeElementPopulationFactory(p -> {
//                    TODO need test for queryFirst and Force create
            return new BuilderValueProducer<>(traitsSpec.toBuilder(jFactory, null), false);
        });
        return collectionProducer;
    }

    private CollectionProducer<?, ?> processElementBuilder(JFactory jFactory, ObjectFactory<?> factory, CollectionProducer<?, ?> collectionProducer) {
        subBuilders(factory).map(subBuilder -> force ? subBuilder.forceCreate() : subBuilder).forEach((subBuilder) ->
                collectionProducer.changeChild(subBuilder.property(), subBuilder.buildProducer(collectionProducer, factory, jFactory)));
        return collectionProducer;
    }

    @Override
    protected SubBuilder mergeTo(SubBuilder to) {
        return to.mergeFrom(this);
    }

    @Override
    protected SubBuilder mergeFrom(SubCollectionBuilder from) {
//        TODO merge force ?
        SubCollectionBuilder subCollectionBuilder = new SubCollectionBuilder(property(),
                traitsSpec.mergeFrom(from.traitsSpec, property()), force || from.force, parentCollectionBuilder(), queryFirst);
        subCollectionBuilder.subProperties.putAll(from.subProperties);
        subCollectionBuilder.subProperties.putAll(subProperties);
        return subCollectionBuilder;
    }

    @Override
    public SubBuilder forceCreate() {
        SubCollectionBuilder newBuilder = new SubCollectionBuilder(property(), traitsSpec, true, parentCollectionBuilder(), queryFirst);
        newBuilder.subProperties.putAll(subProperties);
        return newBuilder;
    }

    @Override
    public boolean matches(Object object, ObjectFactory<?> objectFactory) {
        if (force)
            return false;
        Object propertyValue = BeanClass.createFrom(object).getPropertyValue(object, property());
        KeyValueCollection.Matcher2 objectMatcher2 = new KeyValueCollection.Matcher2<>(subBuilders(objectFactory).collect(Collectors.toList()));
        return objectMatcher2.matches(propertyValue, objectFactory);
    }
}
