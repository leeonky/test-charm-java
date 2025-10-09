package com.github.leeonky.jfactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TypeSequence {
    private int start = 1;
    private final Map<Class<?>, AtomicInteger> sequences = new HashMap<>();

    public Sequence register(Class<?> type) {
        return new Sequence(type);
    }

    public void reset() {
        sequences.clear();
    }

    public void setStart(int start) {
        this.start = start;
    }

    public class Sequence {
        private final Class<?> type;
        private Integer value;

        public Sequence(Class<?> type) {
            this.type = type;
        }

        public int get() {
            if (value == null)
                value = sequences.computeIfAbsent(type, k -> new AtomicInteger(start)).getAndIncrement();
            return value;
        }
    }
}
