package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.github.leeonky.jfactory.PropertyChain.propertyChain;

class DefaultListConsistency<T> implements ListConsistency<T> {
    private final PropertyChain property;
    private final DefaultConsistency<T> consistency;
    private final List<Consumer<Integer>> actions = new ArrayList<>();

    public DefaultListConsistency(String property, DefaultConsistency<T> consistency) {
        this.property = propertyChain(property);
        this.consistency = consistency;
    }

    @Override
    public Consistency<T> direct(String property) {
        actions.add(index -> consistency.direct(combine(index, property)));
        return consistency;
    }

    private String combine(int index, String property) {
        return this.property.toString() + String.format("[%d].", index) + property;
    }

    public void resolveToItems(ObjectProducer<?> producer) {
        Producer<?> descendant = producer.descendant(property);
        if (descendant instanceof CollectionProducer) {
            CollectionProducer<?, ?> collectionProducer = (CollectionProducer) descendant;
            int count = collectionProducer.childrenCount();
            for (int i = 0; i < count; i++) {
                int index = i;
                actions.forEach(action -> action.accept(index));
            }
        } else
            throw new IllegalStateException();
    }
}
