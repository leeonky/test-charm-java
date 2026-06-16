package org.testcharm.dal.ast.opt;

import org.testcharm.dal.ast.node.DALExpression;
import org.testcharm.dal.ast.node.DALNode;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.Operators;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.interpreter.Operator;

import static org.testcharm.util.Strings.nullOrEmpty;

public abstract class DALOperator extends Operator<DALRuntimeContext, DALNode, DALOperator, DALExpression> {
    private final boolean needInspect;
    private final Operators type;

    protected DALOperator(int precedence, String label, boolean needInspect, Operators type) {
        super(precedence, label);
        this.needInspect = needInspect;
        this.type = type;
    }

    public Data<?> calculateData(DALExpression expression, DALRuntimeContext context) {
        return context.data(calculate(expression, context));
    }

    @Override
    public Object calculate(DALExpression expression, DALRuntimeContext context) {
        return calculateData(expression, context).value();
    }

    public boolean isNeedInspect() {
        return needInspect;
    }

    public String inspect(String node1, String node2) {
        if (nullOrEmpty(node1))
            return String.format("%s %s", label, node2);
        return String.format("%s %s %s", node1, label, node2);
    }

    public Operators type() {
        return type;
    }
}
