package com.github.leeonky.interpreter;

import java.util.function.UnaryOperator;

public interface MapAble<SELF extends MapAble<SELF, T>, T> {
    SELF map(UnaryOperator<T> mapper);
}
