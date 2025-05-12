package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.DALRuntimeException;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.interpreter.Node;

import static com.github.leeonky.dal.runtime.DALException.locateError;
import static com.github.leeonky.dal.runtime.ExpressionException.opt1;

public interface ExecutableNode extends Node<RuntimeContextBuilder.DALRuntimeContext, DALNode> {

    Data<?> getValue(Data<?> data, RuntimeContextBuilder.DALRuntimeContext context);

    default Data<?> getValue(DALNode left, RuntimeContextBuilder.DALRuntimeContext context) {
        return getValue(evaluateInput(left, context), context);
    }

    default Data<?> evaluateInput(DALNode left, RuntimeContextBuilder.DALRuntimeContext context) {
        Data<?> data = left.evaluateData(context);
        if (opt1(data::isNull))
            throw locateError(new DALRuntimeException("Instance is null"), getOperandPosition());
        return data;
    }
}
