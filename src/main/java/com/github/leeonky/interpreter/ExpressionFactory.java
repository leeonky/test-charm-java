package com.github.leeonky.interpreter;

public interface ExpressionFactory<C extends RuntimeContext, N extends Node<C, N>, E extends Expression<C, N, E, O>,
        O extends Operator<C, N, O, E>> {
    E create(N left, O operator, N right);
}
