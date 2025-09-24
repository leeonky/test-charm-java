package com.github.leeonky.jfactory;

import java.util.HashSet;
import java.util.Set;

class DecomposerExecutor<T> {
    private final Consistency.Decomposer<T> decomposer;
    private final DefaultConsistency<T>.Executor executor;
    private Object[] composed;
    private final Set<DecomposerExecutor<T>> stacks = new HashSet<>();

    public DecomposerExecutor(Consistency.Decomposer<T> decomposer,
                              DefaultConsistency<T>.Executor executor) {
        this.decomposer = decomposer;
        this.executor = executor;
    }

    @SuppressWarnings("unchecked")
    public T getValue(int index) throws ConsistencyCircularityException {
        if (composed == null) {
            if (stacks.contains(this))
                throw new ConsistencyCircularityException();
            stacks.add(this);
            composed = decomposer.apply(executor.compose());
        }
        return (T) composed[index];
    }

}