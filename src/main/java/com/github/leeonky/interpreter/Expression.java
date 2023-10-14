package com.github.leeonky.interpreter;

public interface Expression<C extends RuntimeContext, N extends Node<C, N>, E extends Expression<C, N, E, O>,
        O extends Operator<C, N, O>> extends Node<C, N> {

    N getLeftOperand();

    N getRightOperand();

    O getOperator();

    @SuppressWarnings("unchecked")
    default N applyPrecedence(ExpressionFactory<C, N, E, O> factory) {
        if (getLeftOperand() instanceof Expression) {
            E leftExpression = (E) getLeftOperand();
            if (getOperator().isPrecedentThan(leftExpression.getOperator()))
                return (N) factory.create(leftExpression.getLeftOperand(), leftExpression.getOperator(),
                        factory.create(leftExpression.getRightOperand(), getOperator(), getRightOperand())
                                .applyPrecedence(factory));
        }
        if (getRightOperand() instanceof Expression) {
            E rightExpression = (E) getRightOperand();
            if (getOperator().isPrecedentThan(rightExpression.getOperator()))
                return (N) factory.create(factory.create(getLeftOperand(), getOperator(),
                                rightExpression.getLeftOperand()).applyPrecedence(factory),
                        rightExpression.getOperator(), rightExpression.getRightOperand());
        }
        return (N) this;
    }
}
