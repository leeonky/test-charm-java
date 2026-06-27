package org.testcharm.dal.ast.node;

import org.testcharm.dal.ast.node.table.RowHeader;
import org.testcharm.dal.ast.node.table.RowType;
import org.testcharm.dal.ast.opt.DALOperator;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.ExpressionException;
import org.testcharm.dal.runtime.Operators;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.interpreter.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.testcharm.dal.runtime.DALException.locateError;

public class DALExpression extends DALNode implements Expression<DALRuntimeContext, DALNode, DALExpression, DALOperator>,
        ExecutableNode {
    private final DALNode left;
    private final DALOperator operator;
    private final DALNode right;

    private DALExpression(DALNode left, DALOperator operator, DALNode right) {
        this.left = left;
        this.right = right;
        this.operator = operator;
        // TODO missing test, executable missing position
        setPositionBegin(operator.getPosition() == 0 ? right.getPositionBegin() : operator.getPosition());
    }

    public static DALNode expression(DALNode left, DALOperator operator, DALNode right) {
        if (left instanceof GroupExpression)
            return ((GroupExpression) left).append(operator, right);
        if (right instanceof GroupExpression)
            return ((GroupExpression) right).insert(left, operator);
        return new DALExpression(left, operator, right).applyPrecedence(DALExpression::new);
    }

    @Override
    public boolean compareLeft(DALExpression leftExpression) {
        if ((leftExpression.operator().type() == Operators.EQUAL || leftExpression.operator().type() == Operators.MATCH)
                && leftExpression.right().improvePrecedence())
            return false;
        return Expression.super.compareLeft(leftExpression);
    }

    @Override
    public DALNode left() {
        return left;
    }

    @Override
    public DALNode right() {
        return right;
    }

    @Override
    public DALOperator operator() {
        return operator;
    }

    @Override
    public Data<?> evaluateData(DALRuntimeContext context) {
        try {
            return operator.calculate(this, context);
        } catch (ExpressionException e) {
            throw e.rethrow(this);
        } catch (Throwable e) {
            throw locateError(e, right().getPositionBegin());
        }
    }

    @Override
    public String inspect() {
        return operator.inspect(left == null ? null : left.inspect(), right.inspect());
    }

    @Override
    public Object getRootSymbolName() {
        return left instanceof InputNode || isRootPropertyThis() ? right.getRootSymbolName() : left.getRootSymbolName();
    }

    private boolean isRootPropertyThis() {
        return left instanceof DALExpression && ((DALExpression) left).right() instanceof PropertyThis;
    }

    @Override
    public int getOperandPosition() {
        return right.getPositionBegin();
    }

    @Override
    public List<Object> propertyChain() {
        return new ArrayList<Object>() {{
            addAll(left.propertyChain());
            addAll(right.propertyChain());
        }};
    }

    @Override
    public Stream<Object> collectFields(Data<?> data) {
        if (((DALExpression) left()).right() instanceof PropertyThis)
            if (right() instanceof ObjectScopeNode)
                return right().collectFields(data);
        return super.collectFields(data);
    }

    @Override
    public Data<?> getValue(Data<?> data, DALRuntimeContext context) {
        return context.pushAndExecute(data, () -> evaluateData(context));
    }

    @Override
    public int getPositionBegin() {
        if (left == null || left instanceof InputNode)
            return super.getPositionBegin();
        return Math.min(super.getPositionBegin(), left.getPositionBegin());
    }

    @Override
    public RowType guessTableHeaderType() {
        if (left() instanceof InputNode && right() instanceof DataRemarkNode)
            return RowHeader.DEFAULT_INDEX;
        return RowHeader.SPECIFY_PROPERTY;
    }

    @Override
    public boolean needPostBlankWarningCheck() {
        if (right instanceof TableNode || right instanceof TransposedTableNode) {
            return false;
        }
        return !isGenerated();
    }

    @Override
    public Boolean isAssertion() {
        return operator().type() == Operators.MATCH || operator().type() == Operators.EQUAL;
    }
}
