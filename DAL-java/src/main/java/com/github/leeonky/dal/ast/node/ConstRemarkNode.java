package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.DALRuntimeException;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

import java.util.Objects;

import static com.github.leeonky.dal.runtime.DALException.locateError;

public class ConstRemarkNode extends DALNode {
    private final DALNode constNode;
    private final DALNode remarkNode;

    public ConstRemarkNode(DALNode constNode, DALNode remarkNode) {
        this.constNode = constNode;
        this.remarkNode = remarkNode;
    }

    @Override
    public String inspect() {
        return constNode.inspect() + " " + remarkNode.inspect();
    }

    @Override
    public Data<?> evaluateData(RuntimeContextBuilder.DALRuntimeContext context) {
        Data<?> leftValue = constNode.evaluateData(context);
        Data<?> rightValue = remarkNode.evaluateData(context);
        if (Objects.equals(leftValue.value(), rightValue.value()))
            return leftValue;
        throw locateError(new DALRuntimeException(String.format("Incorrect const remark, const value was %s\nbut remark %s was %s",
                leftValue.dump(), remarkNode.inspect(), rightValue.dump())), remarkNode.getPositionBegin());
    }
}
