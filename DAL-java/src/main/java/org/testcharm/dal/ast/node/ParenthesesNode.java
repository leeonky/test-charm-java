package org.testcharm.dal.ast.node;

import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.RuntimeContextBuilder;

public class ParenthesesNode extends DALNode {
    private final DALNode node;

    public ParenthesesNode(DALNode node) {
        this.node = node;
    }

    @Override
    public Data<?> evaluateData(RuntimeContextBuilder.DALRuntimeContext context) {
        return node.evaluateData(context);
    }

    @Override
    public String inspect() {
        return "(" + node.inspect() + ")";
    }

    @Override
    public int getOperandPosition() {
        return node.getPositionBegin();
    }
}
