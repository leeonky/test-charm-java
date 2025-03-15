package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.DalException;
import com.github.leeonky.dal.runtime.DalRuntimeException;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.interpreter.InterpreterException;
import com.github.leeonky.interpreter.Node;

import static com.github.leeonky.dal.runtime.DalException.locateError;
import static com.github.leeonky.dal.runtime.ExpressionException.exception;

public interface ExecutableNode extends Node<RuntimeContextBuilder.DALRuntimeContext, DALNode> {

    Data getValue(Data data, RuntimeContextBuilder.DALRuntimeContext context);

    default Data getValue(DALNode left, RuntimeContextBuilder.DALRuntimeContext context) {
        Data data = left.evaluateData(context);
        data.peek(e -> {
            if (context.isNull(e.value()))
                throw locateError(new DalRuntimeException("Instance is null"), getOperandPosition());
        }).onError(e -> {
            if (e instanceof InterpreterException) {
                return e;
            }
            return exception(expression -> new DalException(expression.left().getOperandPosition(), e));
        });
        return getValue(data, context);
    }
}
