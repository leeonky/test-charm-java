package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.DalRuntimeException;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.interpreter.Node;

import static com.github.leeonky.dal.runtime.DalException.locateError;
import static com.github.leeonky.dal.runtime.ExpressionException.opt1;

public interface ExecutableNode extends Node<RuntimeContextBuilder.DALRuntimeContext, DALNode> {

    Data getValue(Data data, RuntimeContextBuilder.DALRuntimeContext context);

    default Data getValue(DALNode left, RuntimeContextBuilder.DALRuntimeContext context) {
        Data data = left.evaluateData(context);
        if (opt1(data::isNull))
            throw locateError(new DalRuntimeException("Instance is null"), getOperandPosition());
        return getValue(data, context);
    }
}
