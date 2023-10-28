package com.github.leeonky.interpreter;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpressionTest {

    @Nested
    class ApplyPrecedence {

        @Test
        void do_nothing_when_left_operand_is_not_expression() {
            TestExpression testExpression = new TestExpression(new TestNode(), null, null);

            assertThat(testExpression.applyPrecedence(null)).isSameAs(testExpression);
        }

        @Test
        void do_nothing_when_left_operand_is_expression_with_higher_precedence_of_current_expression_opt() {
            TestExpression testExpression = new TestExpression(new TestExpression(null, new TestOperator(2), null),
                    new TestOperator(1), null);

            assertThat(testExpression.applyPrecedence(null)).isSameAs(testExpression);
        }

        @Test
        void change_expression_when_current_expression_has_higher_precedence() {
            TestNode left = new TestNode();
            TestNode right = new TestNode();
            TestNode node3 = new TestNode();
            TestOperator operator1 = new TestOperator(1);
            TestOperator operator2 = new TestOperator(2);
            TestExpression testExpression = new TestExpression(new TestExpression(left, operator1, right),
                    operator2, node3);

            TestExpression newExpression = (TestExpression) testExpression.applyPrecedence((TestExpression::new));

            assertThat(newExpression.left()).isSameAs(left);
            assertThat(newExpression.operator()).isSameAs(operator1);

            TestExpression rightOperand = (TestExpression) newExpression.right();
            assertThat(rightOperand.left()).isSameAs(right);
            assertThat(rightOperand.operator()).isSameAs(operator2);
            assertThat(rightOperand.right()).isSameAs(node3);
        }

        @Test
        void change_expression_when_current_expression_has_higher_precedence_than_right_operand_expression() {
            TestNode left = new TestNode();
            TestNode rightLeft = new TestNode();
            TestNode rightRight = new TestNode();
            TestOperator operator10 = new TestOperator(10);
            TestOperator operator9 = new TestOperator(9);
            TestExpression testExpression = new TestExpression(left,
                    operator10, new TestExpression(rightLeft, operator9, rightRight));

            TestExpression newExpression = (TestExpression) testExpression.applyPrecedence(TestExpression::new);

            TestExpression leftExpression = (TestExpression) newExpression.left();
            assertThat(leftExpression.left()).isSameAs(left);
            assertThat(leftExpression.operator()).isSameAs(operator10);
            assertThat(leftExpression.right()).isSameAs(rightLeft);

            assertThat(newExpression.operator()).isSameAs(operator9);

            assertThat(newExpression.right()).isSameAs(rightRight);
        }
    }
}