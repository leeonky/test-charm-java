package com.github.leeonky.jfactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TypeSequence {
    private final Map<Class<?>, AtomicInteger> sequences = new HashMap<>();

    public Sequence register(Class<?> type) {
        return new Sequence(type);
    }

    public class Sequence {
        private final Class<?> type;
        private Integer value;

        public Sequence(Class<?> type) {
            this.type = type;
        }

        public int get() {
            if (value == null)
                value = sequences.computeIfAbsent(type, k -> new AtomicInteger(0)).incrementAndGet();
            return value;
        }
    }
}
