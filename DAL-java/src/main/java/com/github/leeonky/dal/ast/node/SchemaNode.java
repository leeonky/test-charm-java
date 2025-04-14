package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.*;

import static com.github.leeonky.dal.runtime.DalException.locateError;

public class SchemaNode extends DALNode {
    private final String schema;

    public SchemaNode(String schema) {
        this.schema = schema;
    }

    @Override
    public String inspect() {
        return schema;
    }

    public Object convertViaSchema(RuntimeContextBuilder.DALRuntimeContext context, Data inputData, String inputProperty) {
        try {
            return context.searchValueConstructor(schema).orElseThrow(() -> locateError(
                            new DalRuntimeException("Schema '" + schema + "' not registered"), getPositionBegin()))
                    .apply(inputData, context);
        } catch (IllegalTypeException exception) {
            throw new AssertionFailure(exception.assertionFailureMessage(inputProperty.isEmpty() ?
                    inputProperty : inputProperty + " ", this), getPositionBegin());
        }
    }
}
