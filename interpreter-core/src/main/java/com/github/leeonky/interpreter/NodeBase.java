package com.github.leeonky.interpreter;

public abstract class NodeBase<C extends RuntimeContext, N extends NodeBase<C, N>> implements Node<C, N> {
    protected int positionBegin, indent;

    @Override
    public int getPositionBegin() {
        return positionBegin;
    }

    @Override
    @SuppressWarnings("unchecked")
    public N setPositionBegin(int positionBegin) {
        this.positionBegin = positionBegin;
        return (N) this;
    }

    @Override
    public int getOperandPosition() {
        return getPositionBegin();
    }

    @Override
    @SuppressWarnings("unchecked")
    public N setIndent(int indent) {
        this.indent = indent;
        return (N) this;
    }

    @Override
    public int getIndent() {
        return indent;
    }
}
