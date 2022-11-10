package com.github.leeonky.util.function;

public interface TriFunction<T1, T2, T3, R> {
    R apply(T1 obj1, T2 obj2, T3 obj3);
}
