package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.ConstructorViaSchema;
import com.github.leeonky.dal.runtime.DalRuntimeException;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

public class SchemaNode extends DALNode {
    private final String schema;

    public SchemaNode(String schema) {
        this.schema = schema;
    }

    public ConstructorViaSchema getValueConstructorViaSchema(RuntimeContextBuilder.DALRuntimeContext context) {
        return context.searchValueConstructor(schema).orElseThrow(() ->
                new DalRuntimeException("Schema '" + schema + "' not registered").toDalError(getPositionBegin()));
    }

    @Override
    public String inspect() {
        return schema;
    }
}
