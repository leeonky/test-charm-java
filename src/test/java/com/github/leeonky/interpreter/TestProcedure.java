package com.github.leeonky.interpreter;

public class TestProcedure extends Procedure<TestContext, TestNode, TestExpression, TestOperator> {
    public TestProcedure(SourceCode sourceCode) {
        super(sourceCode, null);
    }

    @Override
    public TestNode createExpression(TestNode node1, TestOperator operator, TestNode node2) {
        return new TestExpression(node1, operator, node2).applyPrecedence(TestExpression::new);
    }
}
