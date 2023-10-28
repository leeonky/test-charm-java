package com.github.leeonky.interpreter;

public interface Expression<C extends RuntimeContext, N extends Node<C, N>, E extends Expression<C, N, E, O>,
        O extends Operator<C, N, O, E>> extends Node<C, N> {

    N left();

    N right();

    O operator();

    @SuppressWarnings("unchecked")
    default N applyPrecedence(ExpressionFactory<C, N, E, O> factory) {
        if (left() instanceof Expression) {
            E leftExpression = (E) left();
            if (operator().isPrecedentThan(leftExpression.operator()))
                return (N) factory.create(leftExpression.left(), leftExpression.operator(),
                        factory.create(leftExpression.right(), operator(), right())
                                .applyPrecedence(factory));
        }
        if (right() instanceof Expression) {
            E rightExpression = (E) right();
            if (operator().isPrecedentThan(rightExpression.operator()))
                return (N) factory.create(factory.create(left(), operator(),
                                rightExpression.left()).applyPrecedence(factory),
                        rightExpression.operator(), rightExpression.right());
        }
        return (N) this;
    }
}
