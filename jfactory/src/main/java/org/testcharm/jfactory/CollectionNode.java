package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;
import org.testcharm.util.PropertyWriter;

import java.util.List;
import java.util.stream.Collectors;

import static org.testcharm.util.CollectionHelper.reify;

class CollectionNode extends CompositeBuilder {
    private final CollectionNode parent;

    public CollectionNode(String property, TraitsSpec traitsSpec, boolean force, CollectionNode parent) {
        super(property, traitsSpec, force);
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    // TODO missing test [-1] [3] should process positive index first?  but [0] [-2] also incorrect
    public Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory) {
        if (traitsSpec.spec() != null) {
            return traitsSpec.isCollectionElementSpec() ?
                    processElementNode(jFactory, factory, generateCollectionProducerByInputSpec(parent, factory, jFactory))
                    : new BuilderValueProducer<>(traitsSpec.toBuilder(jFactory, null), false).apply(subProperties);
        } else {
            Producer<?> producer = parent.getChildOrDefaultCollection((PropertyWriter) parent.getType().getPropertyWriter(property()));
            return producer instanceof CollectionProducer
                    ? processElementNode(jFactory, factory, (CollectionProducer<?, ?>) producer)
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

    private CollectionProducer<?, ?> processElementNode(JFactory jFactory, ObjectFactory<?> factory, CollectionProducer<?, ?> collectionProducer) {
        createSubNodes(factory).map(node -> force ? node.forceCreate() : node).forEach((node) ->
                collectionProducer.changeChild(node.property(), node.buildProducer(collectionProducer, factory, jFactory)));
        return collectionProducer;
    }

    @Override
    protected PropertyNode mergeTo(PropertyNode to) {
        return to.mergeFrom(this);
    }

    @Override
    protected PropertyNode mergeFrom(CollectionNode from) {
//        TODO merge force ?
        CollectionNode collectionNode = new CollectionNode(property(),
                traitsSpec.mergeFrom(from.traitsSpec, property()), force || from.force, parent);
        collectionNode.subProperties.putAll(from.subProperties);
        collectionNode.subProperties.putAll(subProperties);
        return collectionNode;
    }

    @Override
    public PropertyNode forceCreate() {
        CollectionNode newBuilder = new CollectionNode(property(), traitsSpec, true, parent);
        newBuilder.subProperties.putAll(subProperties);
        return newBuilder;
    }

    @Override
    public boolean matches(Object object, ObjectFactory<?> objectFactory) {
        if (force)
            return false;
        Object propertyValue = BeanClass.createFrom(object).getPropertyValue(object, property());
        Matcher objectMatcher = new Matcher<>(createSubNodes(objectFactory).collect(Collectors.toList()));
        return objectMatcher.matches(propertyValue, objectFactory);
    }
}
