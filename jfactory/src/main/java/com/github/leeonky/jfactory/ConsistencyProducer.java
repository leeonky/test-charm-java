package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.List;

public class ConsistencyProducer<T> extends Producer<T> {
    private final Producer<T> originProducer;
    private final int index;
    private final List<DecomposerExecutor<T>> decomposerExecutors = new ArrayList<>();

    public ConsistencyProducer(Producer<T> originProducer, int index, DecomposerExecutor<T> decomposerExecutor) {
        super(originProducer.getType());
        this.originProducer = originProducer;
        this.index = index;
        decomposerExecutors.add(decomposerExecutor);
    }

    @Override
    protected T produce() {
        try {
            return decomposerExecutors.get(0).getValue(index);
        } catch (ConsistencyCircularityException e) {
            return originProducer.getValue();
        }
    }
}

//TODO real ConsistencyProducer
//TODO origin is also a SubConsistentProducer
//TODO resolve order: fixed, readonly, value, default
//TODO changeDescendant consistentProducer to readonly
//TODO stackoverflow
