package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.Data.Resolved;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.interpreter.Node;

import static com.github.leeonky.dal.runtime.ExpressionException.locatedError;
import static com.github.leeonky.dal.runtime.ExpressionException.testOpt1;

public interface ExecutableNode extends Node<RuntimeContextBuilder.DALRuntimeContext, DALNode> {

    Data getValue(Data data, RuntimeContextBuilder.DALRuntimeContext context);

    default Data getValue(DALNode left, RuntimeContextBuilder.DALRuntimeContext context) {
        Data data = left.evaluateData(context).when(testOpt1(Resolved::isNull))
                .thenThrow(locatedError("Instance is null", getOperandPosition()));
        return getValue(data, context);
    }
}
