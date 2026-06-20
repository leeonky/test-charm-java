package org.testcharm.dal.ast.node;

import org.testcharm.dal.ast.node.table.RowHeader;
import org.testcharm.dal.ast.node.table.RowType;
import org.testcharm.dal.ast.opt.DALOperator;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.ExpectationFactory;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.interpreter.NodeBase;

import java.util.List;
import java.util.stream.Stream;

public abstract class DALNode extends NodeBase<DALRuntimeContext, DALNode> {
    private boolean generated = false;

    public Data<?> evaluateData(DALRuntimeContext context) {
        return context.data(evaluate(context));
    }

    @Override
    public Object evaluate(DALRuntimeContext context) {
        return evaluateData(context).value();
    }

    public abstract String inspect();

    public Object getRootSymbolName() {
        return null;
    }

    public List<Object> propertyChain() {
        throw new IllegalStateException();
    }

    public Stream<Object> collectFields(Data<?> data) {
        return Stream.of(data.firstFieldFromAlias(getRootSymbolName()));
    }

    public Data<?> verify(DALOperator operator, DALNode actual, DALRuntimeContext context) {
        Data<?> actualData = actual.evaluateData(context);
        return context.pushPositionAndExecute(actual.getOperandPosition(),
                () -> context.calculate(actualData, operator, context.data(toVerify(context))));
    }

    protected ExpectationFactory toVerify(DALRuntimeContext context) {
        Data<?> expected = evaluateData(context);
        return (operator, actual) -> new ExpectationFactory.Expectation() {
            @Override
            public Data<?> matches() {
                return context.fetchMatchingChecker(expected, actual).verify(expected, actual, context);
            }

            @Override
            public Data<?> equalTo() {
                return context.fetchEqualsChecker(expected, actual).verify(expected, actual, context);
            }

            @Override
            public ExpectationFactory.Type type() {
                return ExpectationFactory.Type.VALUE;
            }
        };
    }

    public RowType guessTableHeaderType() {
        return RowHeader.DEFAULT_INDEX;
    }

    public boolean needPrefixBlankWarningCheck() {
        return false;
    }

    public boolean needPostBlankWarningCheck() {
        return false;
    }

    @Override
    public String toString() {
        return inspect();
    }

    public DALNode markGenerated() {
        generated = true;
        return this;
    }

    public boolean isGenerated() {
        return generated;
    }

    public Boolean isAssertion() {
        return false;
    }

    public boolean improvePrecedence() {
        return false;
    }
}
