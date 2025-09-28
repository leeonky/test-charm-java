package com.github.leeonky.jfactory;

import static com.github.leeonky.jfactory.PropertyChain.propertyChain;

class DefaultListConsistency<T> implements ListConsistency<T> {
    private final PropertyChain listProperty;
    private final DefaultConsistency<T> consistency;
    private String property;
    private AbstractConsistency.Composer<T> composer;
    private AbstractConsistency.Decomposer<T> decomposer;

    public DefaultListConsistency(String listProperty, DefaultConsistency<T> consistency) {
        this.listProperty = propertyChain(listProperty);
        this.consistency = consistency;
    }

    @Override
    public Consistency<T> direct(String property) {
        this.<T>property(property).read(s -> s).write(s -> s);
        return consistency;
    }

    public void setComposer(ComposerWrapper<T> composer) {
        this.composer = composer;
    }

    public void setDecomposer(DecomposerWrapper<T> decomposer) {
        this.decomposer = decomposer;
    }

    @Override
    public <P> AbstractConsistency.LC1<T, P> property(String property) {
        this.property = property;
        return new AbstractConsistency.LC1<>(consistency, this);
    }

    private String combine(int index, String property) {
        return listProperty.toString() + String.format("[%d].", index) + property;
    }

    public void resolveToItems(ObjectProducer<?> producer) {
        Producer<?> descendant = producer.descendant(listProperty);
        if (descendant instanceof CollectionProducer) {
            CollectionProducer<?, ?> collectionProducer = (CollectionProducer) descendant;
            int count = collectionProducer.childrenCount();
            for (int i = 0; i < count; i++) {
                int index = i;
                consistency.property(combine(index, property))
                        .read(s -> composer.apply(new Object[]{s}))
                        .write(t -> decomposer.apply(t)[0]);
            }
        } else
            throw new IllegalStateException();
    }
}
