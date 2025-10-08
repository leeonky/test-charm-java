package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.List;

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
                int index = i;
                producer.getChild(String.valueOf(index)).ifPresent(p -> {
                    if (!(p instanceof DefaultValueProducer)) {
                        coordinates.add(new Coordinate(singletonList(new Index(producer.childrenCount(), index))));
                    }
                });
            }
            return coordinates;
        }

        public void populate(List<Coordinate> coordinates, ObjectProducer<T> objectProducer, JFactory jFactory) {
            for (Coordinate coordinate : coordinates) {
                objectProducer.changeDescendant(property.concat(coordinate.indexes().get(0).index()),
                        (p, s) -> jFactory.type(p.getType().getElementType()).createProducer());
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
