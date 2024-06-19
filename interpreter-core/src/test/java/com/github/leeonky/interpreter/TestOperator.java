package com.github.leeonky.interpreter;

public class TestOperator extends Operator<TestContext, TestNode, TestOperator, TestExpression> {

    public TestOperator() {
        super(0, "");
    }

    public TestOperator(int precedence) {
        super(precedence, "");
    }

    @Override
    public Object calculate(TestExpression expression, TestContext context) {
        return null;
    }
}
