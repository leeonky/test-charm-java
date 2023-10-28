package com.github.leeonky.interpreter;

public abstract class Operator<C extends RuntimeContext, N extends Node<C, N>, O extends Operator<C, N, O, E>,
        E extends Expression<C, N, E, O>> {
    protected final int precedence;
    protected final String label;
    private int position;

    public Operator(int precedence, String label) {
        this.precedence = precedence;
        this.label = label;
    }

    public boolean isPrecedentThan(O operator) {
        return precedence > operator.precedence;
    }

    public abstract Object calculate(E expression, C context);

    public int getPosition() {
        return position;
    }

    @SuppressWarnings("unchecked")
    public O setPosition(int position) {
        this.position = position;
        return (O) this;
    }
}
