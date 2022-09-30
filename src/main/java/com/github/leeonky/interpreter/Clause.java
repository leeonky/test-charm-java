package com.github.leeonky.interpreter;

public interface Clause<N extends Node<?, N>> {
    N expression(N input);

    default int getOperandPosition(N input) {
        return expression(input).getOperandPosition();
    }
}
