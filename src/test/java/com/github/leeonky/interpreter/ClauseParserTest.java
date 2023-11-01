package com.github.leeonky.interpreter;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.github.leeonky.interpreter.ClauseParser.Mandatory.clause;
import static com.github.leeonky.interpreter.Parser.lazyClause;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClauseParserTest extends BaseTest {

    @Test
    void lazy_parse() {
        ClauseParser<TestNode, TestProcedure> clauseParser =
                mock(ClauseParser.class);
        TestProcedure procedure = givenProcedureWithCode("");

        Clause<TestNode> clause = mock(Clause.class);

        when(clauseParser.parse(procedure)).thenReturn(of(clause));


        assertThat(lazyClause(() -> clauseParser).parse(procedure).get()).isSameAs(clause);
    }

    @Nested
    class Concat {

        @Test
        void concat_with_another_present_clause_parser_should_be_present_clause_parser() {
            TestProcedure givenProcedure = givenProcedureWithCode("");
            TestNode inputNode = new TestNode();
            TestNode firstClauseExpression = new TestNode();
            TestNode lastExpression = new TestNode();

            ClauseParser<TestNode, TestProcedure> clauseParser = procedure -> {
                assertThat(procedure).isSameAs(givenProcedure);
                Clause<TestNode> firstClause = mock(Clause.class);
                when(firstClause.expression(inputNode)).thenReturn(firstClauseExpression);
                return of(firstClause);
            };

            ClauseParser<TestNode, TestProcedure> clauseParser2 = procedure -> {
                assertThat(procedure).isSameAs(givenProcedure);
                Clause<TestNode> nextClause = mock(Clause.class);
                when(nextClause.expression(firstClauseExpression)).thenReturn(lastExpression);
                return of(nextClause);
            };

            assertThat(clauseParser.concat(clauseParser2).parse(givenProcedure).get().expression(inputNode)).isSameAs(lastExpression);
        }

        @Test
        void concat_with_another_empty_clause_parser_should_return_first_part_clause_parser() {
            TestProcedure givenProcedure = givenProcedureWithCode("");

            Clause<TestNode> clause = mock(Clause.class);

            ClauseParser<TestNode, TestProcedure> clauseParser = procedure -> {
                assertThat(procedure).isSameAs(givenProcedure);
                return of(clause);
            };

            ClauseParser<TestNode, TestProcedure> clauseParser2 = procedure -> {
                assertThat(procedure).isSameAs(givenProcedure);
                return empty();
            };

            assertThat(clauseParser.concat(clauseParser2).parse(givenProcedure).get()).isSameAs(clause);
        }

        @Test
        void empty_concat_with_any_clause_parser_should_return_empty() {
            TestProcedure testProcedure = givenProcedureWithCode("");
            ClauseParser<TestNode, TestProcedure> clauseParser = procedure -> {
                assertThat(procedure).isSameAs(testProcedure);
                return empty();
            };

            assertThat(clauseParser.concat(mock(ClauseParser.class)).parse(testProcedure)).isEmpty();
        }
    }

    @Nested
    class ConcatAll {

        @Test
        void concat_with_another_present_clause_parser_should_be_present_clause_parser() {
            TestProcedure givenProcedure = givenProcedureWithCode("");
            TestNode inputNode = new TestNode();
            TestNode firstClauseExpression = new TestNode();
            TestNode lastExpression = new TestNode();

            ClauseParser<TestNode, TestProcedure> clauseParser = procedure -> {
                assertThat(procedure).isSameAs(givenProcedure);
                Clause<TestNode> firstClause = mock(Clause.class);
                when(firstClause.expression(inputNode)).thenReturn(firstClauseExpression);
                return of(firstClause);
            };

            ClauseParser<TestNode, TestProcedure> clauseParser2 = procedure -> {
                assertThat(procedure).isSameAs(givenProcedure);
                Clause<TestNode> nextClause = mock(Clause.class);
                when(nextClause.expression(firstClauseExpression)).thenReturn(lastExpression);
                return of(nextClause);
            };

            assertThat(clauseParser.concatAll(clauseParser2).parse(givenProcedure).get().expression(inputNode)).isSameAs(lastExpression);
        }

        @Test
        void concat_with_another_empty_clause_parser_should_return_first_part_clause_parser() {
            TestProcedure givenProcedure = givenProcedureWithCode("");

            Clause<TestNode> clause = mock(Clause.class);

            ClauseParser<TestNode, TestProcedure> clauseParser = procedure -> {
                assertThat(procedure).isSameAs(givenProcedure);
                return of(clause);
            };

            ClauseParser<TestNode, TestProcedure> clauseParser2 = procedure -> {
                assertThat(procedure).isSameAs(givenProcedure);
                return empty();
            };

            assertThat(clauseParser.concatAll(clauseParser2).parse(givenProcedure).get()).isSameAs(clause);
        }

        @Test
        void empty_concat_with_any_clause_parser_should_return_empty() {
            TestProcedure testProcedure = givenProcedureWithCode("");
            ClauseParser<TestNode, TestProcedure> clauseParser = procedure -> {
                assertThat(procedure).isSameAs(testProcedure);
                return empty();
            };

            assertThat(clauseParser.concatAll(mock(ClauseParser.class)).parse(testProcedure)).isEmpty();
        }


        @Test
        void concat_with_another_present_clause_parser_as_many_times_as_possible() {
            TestProcedure givenProcedure = givenProcedureWithCode("");
            TestNode inputNode = new TestNode();
            TestNode firstClauseOperand = new TestNode();
            TestNode secondClauseOperand = new TestNode();
            TestNode thirdClauseOperand = new TestNode();

            ClauseParser<TestNode, TestProcedure> clauseParser = procedure ->
                    of(input -> new TestExpression(input, new TestOperator(), firstClauseOperand));

            AtomicInteger times = new AtomicInteger(0);
            ClauseParser<TestNode, TestProcedure> clauseParser2 = procedure -> {
                switch (times.getAndIncrement()) {
                    case 0:
                        return of(input -> new TestExpression(input, new TestOperator(), secondClauseOperand));
                    case 1:
                        return of(input -> new TestExpression(input, new TestOperator(), thirdClauseOperand));
                    default:
                        return empty();
                }
            };

            TestExpression expression = (TestExpression) clauseParser.concatAll(clauseParser2).parse(givenProcedure).get().expression(inputNode);

            assertThat(((Expression) ((Expression) expression.left()).left()).left()).isSameAs(inputNode);
            assertThat(((Expression) ((Expression) expression.left()).left()).right()).isSameAs(firstClauseOperand);
            assertThat(((Expression) expression.left()).right()).isSameAs(secondClauseOperand);
            assertThat(expression.right()).isSameAs(thirdClauseOperand);
        }
    }

    @Test
    void convert_to_mandatory() {
        TestProcedure testProcedure = givenProcedureWithCode("");
        TestNode node = new TestNode();
        TestNode expression = new TestNode();

        ClauseParser<TestNode, TestProcedure> clauseParser = procedure -> {
            assertThat(procedure).isSameAs(testProcedure);
            Clause<TestNode> clause = mock(Clause.class);
            when(clause.expression(node)).thenReturn(expression);
            return of(clause);
        };

        assertThat(clauseParser.mandatory("").parse(testProcedure).expression(node)).isSameAs(expression);
    }

    @Test
    void convert_to_mandatory_with_mandatory() {
        TestProcedure testProcedure = givenProcedureWithCode("");

        Clause<TestNode> clause = mock(Clause.class);
        TestNode node = new TestNode();

        TestNode expression = new TestNode();
        when(clause.expression(node)).thenReturn(expression);

        ClauseParser<TestNode, TestProcedure> clauseParser = procedure -> {
            assertThat(procedure).isSameAs(testProcedure);
            return empty();
        };

        ClauseParser.Mandatory<TestNode, TestProcedure> mandatory = procedure -> {
            assertThat(procedure).isSameAs(testProcedure);
            return clause;
        };

        assertThat(clauseParser.or(mandatory).parse(testProcedure).expression(node)).isSameAs(expression);
    }

    @Test
    void create_clause_mandatory_with_mandatory() {
        TestProcedure testProcedure = givenProcedureWithCode("");
        TestNode node = new TestNode();
        NodeParser.Mandatory<TestNode, TestProcedure> nodeMandatory = procedure -> {
            assertThat(procedure).isSameAs(testProcedure);
            return node;
        };

        TestNode input = new TestNode();
        Function<TestNode, NodeParser.Mandatory<TestNode, TestProcedure>> function = inputNode -> {
            assertThat(inputNode).isSameAs(input);
            return nodeMandatory;
        };

        Clause<TestNode> clause = clause(function).parse(testProcedure);

        assertThat(clause.expression(input)).isSameAs(node);
    }
}