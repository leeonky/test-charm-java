package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;
import org.testcharm.util.PropertyWriter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.testcharm.util.CollectionHelper.reify;

class SubCollectionBuilder extends SubBuilder {
    private final TraitsSpec traitsSpec;
    private final boolean force;
    private final Map<String, Object> elementProperties = new LinkedHashMap<>();

    public SubCollectionBuilder(String property, TraitsSpec traitsSpec, boolean force, String clause, Object value, SubCollectionBuilder parentCollectionBuilder) {
        this(property, traitsSpec, force, parentCollectionBuilder);
        elementProperties.put(clause, value);
    }

    public SubCollectionBuilder(String property, TraitsSpec traitsSpec, boolean force, SubCollectionBuilder parentCollectionBuilder) {
        super(property, parentCollectionBuilder);
        this.traitsSpec = traitsSpec;
        this.force = force;
    }

    @SuppressWarnings("unchecked")
    @Override
    // TODO missing test [-1] [3] should process positive index first?  but [0] [-2] also incorrect
    public Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory,
                                     BeanClass<?> collectionSpecElementType) {
        PropertyWriter propertyWriter = parent.getType().getPropertyWriter(property());
        if (traitsSpec.spec() != null) {
            if (traitsSpec.isCollectionElementSpec()) {
                BeanClass<Object> reify = reify(
                        propertyWriter.getType().isCollection() ? propertyWriter.getType().getType() :
                                List.class, traitsSpec.guessPropertyType(factory.getFactorySet()).get().getGenericType());
                propertyWriter = propertyWriter.decorateType(reify);
            } else
                propertyWriter = propertyWriter.decorateType(traitsSpec.guessPropertyType(factory.getFactorySet()).get());
        }
        Producer producer = parent.getChildOrDefaultCollection(propertyWriter);
        if (producer instanceof CollectionProducer) {
            CollectionProducer collectionProducer = (CollectionProducer) producer;
            if (traitsSpec.spec() != null) {
                collectionProducer.changeElementPopulationFactory(p -> {
                    if (traitsSpec.isCollectionElementSpec()) {
                        return new BuilderValueProducer<>(traitsSpec.toBuilder(jFactory, null), false);
                    } else {
                        BeanClass<?> elementType = traitsSpec.guessPropertyType(factory.getFactorySet()).get().getElementType();
                        return new BuilderValueProducer<>(jFactory.type(elementType), false);
                    }
                });
            }
            elementProperties.entrySet().stream().map(e -> SubBuilder.create(e.getKey(), e.getValue(), this))
                    .collect(Collectors.groupingBy(SubBuilder::property, LinkedHashMap::new, Collectors.toList())).values().stream()
                    .map(subBuilders -> subBuilders.stream().reduce(SubBuilder::mergeTo))
                    .filter(Optional::isPresent).map(Optional::get).forEach((subBuilder) -> {
                        if (force) {
                            if (subBuilder instanceof SubObjectBuilder)
                                collectionProducer.changeChild(subBuilder.property(), ((SubObjectBuilder) subBuilder).forceCreate().buildProducer(collectionProducer, factory, jFactory, traitsSpec.resolveElementType(factory.factorySet)));
                            else
                                collectionProducer.changeChild(subBuilder.property(), subBuilder.buildProducer(collectionProducer, factory, jFactory, traitsSpec.resolveElementType(factory.factorySet)));
//                TODO missing case of SubCollectionBuilder, but it is not supported to force create collection element now, so it is not implemented
                        } else {
                            collectionProducer.changeChild(subBuilder.property(), subBuilder.buildProducer(collectionProducer, factory, jFactory, traitsSpec.resolveElementType(factory.factorySet)));
                        }
                    });
            return collectionProducer;
        } else {
            if (traitsSpec.spec() != null) {
                return new BuilderValueProducer<>(traitsSpec.toBuilder(jFactory, null), false).apply(elementProperties);
            } else {
                return ((BuilderValueProducer) producer).apply(elementProperties);
            }
        }
    }

    @Override
    protected SubBuilder mergeTo(SubBuilder to) {
        return to.mergeFrom(this);
    }

    @Override
    protected SubBuilder mergeFrom(SubCollectionBuilder from) {
//        TODO merge force ?
        traitsSpec.mergeFrom(from.traitsSpec, property());

        Map<String, Object> mergedElementProperties = new LinkedHashMap<>();
        mergedElementProperties.putAll(from.elementProperties);
        mergedElementProperties.putAll(elementProperties);
        elementProperties.clear();
        elementProperties.putAll(mergedElementProperties);
        return this;
    }

    //TODO refactor
    public SubBuilder forceCreateElement() {
        SubCollectionBuilder newBuilder = new SubCollectionBuilder(property(), traitsSpec, true, null);
        newBuilder.elementProperties.putAll(elementProperties);
        return newBuilder;
    }
}
