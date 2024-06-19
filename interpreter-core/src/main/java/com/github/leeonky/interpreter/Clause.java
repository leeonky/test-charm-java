package com.github.leeonky.interpreter;

public interface Clause<N extends Node<?, N>> {
    N expression(N input);

    default int getOperandPosition(N input) {
        return expression(input).getOperandPosition();
    }

    default Clause<N> merge(Clause<N> another) {
        return input -> another.expression(expression(input));
    }
}
