package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.ast.node.DALNode;

public class ExclamationData<T> extends RuntimeData<T> {
    private final String label;

    public ExclamationData(Data<T> data, DALNode operandNode, RuntimeContextBuilder.DALRuntimeContext runtimeContext) {
        super(data, runtimeContext);
        label = operandNode.inspect();
    }

    public String label() {
        return label;
    }
}
