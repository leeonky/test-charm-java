package com.github.leeonky.dal.ast;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

public class SchemaWhichExpression extends Node {
    private final SchemaExpression schemaExpression;
    private final Node clause;
    private final boolean omitWhich;

    public SchemaWhichExpression(SchemaExpression schemaExpression, Node clause, boolean omitWhich) {
        this.schemaExpression = schemaExpression;
        this.clause = clause;
        this.omitWhich = omitWhich;
    }

    @Override
    public String inspect() {
        return schemaExpression.inspect() + " " + (omitWhich ? "" : "which ") + clause.inspect();
    }

    @Override
    public Object evaluate(RuntimeContextBuilder.RuntimeContext context) {
        try {
            return context.newThisScope(schemaExpression.evaluateDataObject(context), () -> clause.evaluate(context));
        } catch (IllegalStateException e) {
            throw new RuntimeException(e.getMessage(), getPositionBegin());
        }
    }
}
