package com.github.leeonky.dal.ast;

import com.github.leeonky.dal.CompilingContext;

import java.util.Objects;

public class Expression implements Node {
    private final Node node1, node2;

    private final com.github.leeonky.dal.ast.Operator operator;

    public Expression(Node node1, Node node2, com.github.leeonky.dal.ast.Operator operator) {
        this.node1 = node1;
        this.node2 = node2;
        this.operator = operator;
    }

    @Override
    public Object evaluate(CompilingContext context) {
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Expression
                && Objects.equals(node1, ((Expression) obj).node1)
                && Objects.equals(node2, ((Expression) obj).node2)
                && Objects.equals(operator, ((Expression) obj).operator)
                && Objects.equals(operator, ((Expression) obj).operator);
    }
}
