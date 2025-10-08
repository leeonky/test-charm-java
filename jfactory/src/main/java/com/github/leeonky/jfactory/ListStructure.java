package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.leeonky.jfactory.PropertyChain.propertyChain;
import static java.util.Collections.singletonList;

public class ListStructure<T, C extends Coordinate> {
    private final List<Item> items = new ArrayList<>();

    public ListStructure<T, C> list(String property) {
        Item item = new Item(property);
        items.add(item);
        return this;
    }

    class Item {
        private final PropertyChain property;

        Item(String property) {
            this.property = propertyChain(property);
        }

        List<Coordinate> collectElements(ObjectProducer<T> objectProducer) {
            List<Coordinate> coordinates = new ArrayList<>();
            CollectionProducer<?, ?> producer = (CollectionProducer<?, ?>) objectProducer.descendantForRead(property);
            for (int i = 0; i < producer.childrenCount(); i++) {
                Optional<Producer<?>> child = producer.getChild(String.valueOf(i));
                if (child.isPresent() && !(child.get() instanceof DefaultValueProducer))
                    coordinates.add(new Coordinate(singletonList(new Index(producer.childrenCount(), i))));
            }
            return coordinates;
        }

        public void populate(List<Coordinate> coordinates, ObjectProducer<T> objectProducer, JFactory jFactory) {
            for (Coordinate coordinate : coordinates) {
                Producer<?> producer = objectProducer.descendantForUpdate(property);
                Optional<Producer<?>> child = producer.getChild(String.valueOf(coordinate.indexes().get(0).index()));
                if (!child.isPresent() || child.get() instanceof DefaultValueProducer)
                    producer.changeChild(String.valueOf(coordinate.indexes().get(0).index()),
                            jFactory.type(producer.getType().getElementType()).createProducer());
            }
        }
    }

    void process(ObjectProducer<T> objectProducer, JFactory jFactory) {
        for (Item item : items) {
            List<Coordinate> coordinates = item.collectElements(objectProducer);
            for (Item eachItem : items) {
                if (eachItem != item) {
                    eachItem.populate(coordinates, objectProducer, jFactory);
                }
            }
        }
    }
}
