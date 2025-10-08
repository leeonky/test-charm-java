package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.leeonky.jfactory.PropertyChain.propertyChain;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class ListStructure<T, C extends Coordinate> {
    private final List<Item> items = new ArrayList<>();
    private final BeanClass<C> coordinateType;
    private Item last;

    public ListStructure(Class<C> coordinateType) {
        this.coordinateType = BeanClass.create(coordinateType);
    }

    public ListStructure<T, C> list(String property) {
        last = new Item(singletonList(property));
        items.add(last);
        return this;
    }

    public ListStructure<T, C> list(String property1, String property2) {
        last = new Item(asList(property1, property2));
        items.add(last);
        return this;
    }

    public ListStructure<T, C> normalize(Normalizer<C> normalizer) {
        last.normalize(normalizer::align, normalizer::deAlign);
        return this;
    }

    class Item {
        private final List<PropertyChain> properties;
        private Function<Coordinate, C> aligner = this::convert;
        private Function<C, Coordinate> inverseAligner = e -> e;

        void normalize(Function<Coordinate, C> aligner,
                       Function<C, Coordinate> inverseAligner) {
            this.aligner = aligner;
            this.inverseAligner = inverseAligner;
        }

        private C convert(Coordinate e) {
            return e.convertTo(coordinateType);
        }

        Item(List<String> properties) {
            this.properties = properties.stream().map(PropertyChain::propertyChain).collect(Collectors.toList());
        }

        List<C> collectElements(ObjectProducer<T> objectProducer) {
            List<C> coordinates = new ArrayList<>();
            return getCoordinates(objectProducer, coordinates, 0, propertyChain(""), new ArrayList<>());
        }

        private List<C> getCoordinates(ObjectProducer<T> objectProducer, List<C> coordinates, int l, PropertyChain base, List<Index> indexes) {
            PropertyChain property = base.concat(properties.get(l++));
            CollectionProducer<?, ?> producer = (CollectionProducer<?, ?>) objectProducer.descendantForUpdate(property);
            for (int i = 0; i < producer.childrenCount(); i++) {
                List<Index> newIndexes = new ArrayList<>(indexes);
                newIndexes.add(new Index(producer.childrenCount(), i));
                if (l < properties.size())
                    getCoordinates(objectProducer, coordinates, l, property.concat(i), newIndexes);
                else {
                    Optional<Producer<?>> child = producer.getChild(String.valueOf(i));
                    if (child.isPresent() && !(child.get() instanceof DefaultValueProducer)) {
                        C aligned = aligner.apply(new Coordinate(newIndexes));
                        if (aligned != null)
                            coordinates.add(aligned);
                    }
                }
            }
            return coordinates;
        }

        public void populate(List<C> coordinates, ObjectProducer<T> objectProducer, JFactory jFactory) {
            for (C coordinate : coordinates) {
                Coordinate inverseAligned = inverseAligner.apply(coordinate);
                if (inverseAligned != null) {
                    PropertyChain property = inverseAligned.join(properties);
                    Producer<?> producer = objectProducer.descendantForUpdate(property.removeTail());
                    Optional<Producer<?>> child = producer.getChild(property.tail());
                    if (!child.isPresent() || child.get() instanceof DefaultValueProducer)
                        producer.changeChild(property.tail(),
                                jFactory.type(producer.getType().getElementType()).createProducer());
                }
            }
        }
    }

    void process(ObjectProducer<T> objectProducer, JFactory jFactory) {
        for (Item item : items) {
            List<C> coordinates = item.collectElements(objectProducer);
            for (Item eachItem : items) {
                if (eachItem != item) {
                    eachItem.populate(coordinates, objectProducer, jFactory);
                }
            }
        }
    }
}
