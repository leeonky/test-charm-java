package com.github.leeonky.interpreter;

public interface Node<C extends RuntimeContext, N extends Node<C, N>> {

    default Object evaluate(C context) {
        throw new IllegalStateException();
    }

    int getPositionBegin();

    N setPositionBegin(int positionBegin);

    N setIndent(int indent);

    int getIndent();

    int getOperandPosition();
}
