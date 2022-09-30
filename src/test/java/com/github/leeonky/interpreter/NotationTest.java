package com.github.leeonky.interpreter;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class NotationTest extends BaseTest {
    private int START_POSITION;

    private SourceCode givenSourceCode(String path) {
        SourceCode sourceCode = BaseTest.createSourceCode("prefix" + path);
        sourceCode.popWord(nt("prefix"));
        START_POSITION = 6;
        return sourceCode;
    }

    @Nested
    class Node {
        NodeParser<TestNode, TestProcedure> nodeParser = nt("true").node(TestNode::new);

        @Test
        void return_empty_when_not_match() {
            SourceCode sourceCode = givenSourceCode("not match");

            assertThat(nodeParser.parse(new TestProcedure(sourceCode))).isEmpty();

            assertThat(sourceCode.popChar(emptyMap())).isEqualTo('n');
        }

        @Test
        void trim_left_blank_even_not_match() {
            SourceCode sourceCode = givenSourceCode(" not match");

            assertThat(nodeParser.parse(new TestProcedure(sourceCode))).isEmpty();

            assertThat(sourceCode.popChar(emptyMap())).isEqualTo('n');
        }

        @Test
        void return_node_when_matches() {
            SourceCode sourceCode = givenSourceCode("true");

            Optional<TestNode> testNode = nodeParser.parse(new TestProcedure(sourceCode));

            assertThat(testNode.get().getPositionBegin()).isEqualTo(0 + START_POSITION);
            assertThat(testNode.get().getContent()).isEqualTo("true");
            assertThat(sourceCode.hasCode()).isFalse();
        }

        @Test
        void trim_left_blank_before_matching() {
            SourceCode sourceCode = givenSourceCode(" true");

            Optional<TestNode> testNode = nodeParser.parse(new TestProcedure(sourceCode));

            assertThat(testNode.get().getPositionBegin()).isEqualTo(1 + START_POSITION);
            assertThat(testNode.get().getContent()).isEqualTo("true");
            assertThat(sourceCode.hasCode()).isFalse();
        }
    }

    @Nested
    class WordToken {
        NodeParser<TestNode, TestProcedure> nodeParser = nt("true")
                .wordNode(TestNode::new, new HashSet<>(Arrays.asList("delimiter")));

        @Test
        void return_empty_and_keep_code_position_when_not_match() {
            SourceCode sourceCode = givenSourceCode(" not match");

            assertThat(nodeParser.parse(new TestProcedure(sourceCode))).isEmpty();

            assertThat(sourceCode.popChar(emptyMap())).isEqualTo(' ');
        }

        @Test
        void return_empty_and_keep_code_position_when_not_match_delimiter() {
            SourceCode sourceCode = givenSourceCode(" trueunexpectedDelimiter");

            assertThat(nodeParser.parse(new TestProcedure(sourceCode))).isEmpty();

            assertThat(sourceCode.popChar(emptyMap())).isEqualTo(' ');
        }

        @Test
        void return_node_when_matches() {
            SourceCode sourceCode = givenSourceCode("truedelimiter");

            Optional<TestNode> testNode = nodeParser.parse(new TestProcedure(sourceCode));

            assertThat(testNode.get().getPositionBegin()).isEqualTo(0 + START_POSITION);
            assertThat(testNode.get().getContent()).isEqualTo("true");
            assertThat(sourceCode.popChar(new HashMap<>())).isEqualTo('d');
        }
    }

    @Nested
    class KeywordOperator {
        OperatorParser<TestNode, TestOperator, TestProcedure> operatorParser = nt("and")
                .keywordOperator(TestOperator::new, new HashSet<>(Arrays.asList("delimiter")));

        @Test
        void return_empty_and_keep_code_position_when_not_match() {
            SourceCode sourceCode = givenSourceCode(" not match");

            assertThat(operatorParser.parse(new TestProcedure(sourceCode))).isEmpty();

            assertThat(sourceCode.popChar(emptyMap())).isEqualTo(' ');
        }

        @Test
        void return_empty_and_keep_code_position_when_not_match_delimiter() {
            SourceCode sourceCode = givenSourceCode(" andunexpectedDelimiter");

            assertThat(operatorParser.parse(new TestProcedure(sourceCode))).isEmpty();

            assertThat(sourceCode.popChar(emptyMap())).isEqualTo(' ');
        }

        @Test
        void return_node_when_matches() {
            SourceCode sourceCode = givenSourceCode("anddelimiter");

            Optional<TestOperator> testOperator = operatorParser.parse(new TestProcedure(sourceCode));

            assertThat(testOperator.get().getPosition()).isEqualTo(0 + START_POSITION);
            assertThat(sourceCode.popChar(new HashMap<>())).isEqualTo('d');
        }
    }

    @Nested
    class ClauseTest {

        @Test
        void return_empty_when_not_match() {
            SourceCode sourceCode = givenSourceCode("not match");

            ClauseParser<TestNode, TestProcedure> clauseParser = nt("[]")
                    .clause((token, testNode) -> new TestNode());

            assertThat(clauseParser.parse(new TestProcedure(sourceCode))).isEmpty();

            assertThat(sourceCode.popChar(emptyMap())).isEqualTo('n');
        }


        @Test
        void return_clause_when_matches() {
            TestProcedure procedure = givenProcedureWithCode("  []");
            TestNode inputNode = new TestNode();
            TestNode testNode = new TestNode();

            ClauseParser<TestNode, TestProcedure> clauseParser = nt("[]")
                    .clause((token, input) -> {
                        assertThat(input).isSameAs(inputNode);
                        assertThat(token.getContent()).isEqualTo("[]");
                        return testNode;
                    });

            TestNode expression = clauseParser.parse(procedure).get().expression(inputNode);

            assertThat(expression).isSameAs(testNode);
            assertThat(expression.getPositionBegin()).isEqualTo(2);
        }
    }

    @Nested
    class Operator {

        @Test
        void return_when_match_symbol() {
            TestOperator testOperator = new TestOperator();
            SourceCode sourceCode = givenSourceCode(" +=");
            OperatorParser<TestNode, TestOperator, TestProcedure> operatorParser =
                    nt("+=").operator(() -> testOperator);

            TestOperator testOperator2 = operatorParser.parse(new TestProcedure(sourceCode)).get();

            assertThat(testOperator2).isSameAs(testOperator);
            assertThat(testOperator2.getPosition()).isEqualTo(1 + START_POSITION);
        }

        @Test
        void return_empty_when_not_match() {
            SourceCode sourceCode = givenSourceCode(" +=");
            OperatorParser<TestNode, TestOperator, TestProcedure> operatorParser =
                    nt("++").operator(TestOperator::new);

            assertThat(operatorParser.parse(new TestProcedure(sourceCode))).isEmpty();
            assertThat(sourceCode.popChar(emptyMap())).isEqualTo('+');
        }

        @Test
        void return_empty_when_predicate_false() {
            TestOperator testOperator = new TestOperator();
            SourceCode sourceCode = givenSourceCode(" +=");
            TestProcedure testProcedure = new TestProcedure(sourceCode);

            OperatorParser<TestNode, TestOperator, TestProcedure> operatorParser =
                    nt("+=").operator(() -> testOperator, scanner1 -> {
                        assertThat(scanner1).isSameAs(testProcedure);
                        return false;
                    });

            assertThat(operatorParser.parse(testProcedure)).isEmpty();
            assertThat(sourceCode.popChar(emptyMap())).isEqualTo('+');
        }
    }

    @Nested
    class NotationWithMandatory {
        NodeParser.Mandatory<TestNode, TestProcedure> mandatory =
                nt("a").<TestNode, TestProcedure>node(TestNode::new).mandatory("");

        @Test
        void return_empty_when_not_start_with() {
            SourceCode sourceCode = givenSourceCode("not match");

            assertThat(nt("s").with(mandatory).parse(new TestProcedure(sourceCode))).isEmpty();
            assertThat(sourceCode.popChar(emptyMap())).isEqualTo('n');
        }

        @Test
        void left_trim_even_not_match() {
            SourceCode sourceCode = givenSourceCode(" not match");

            assertThat(nt("s").with(mandatory).parse(new TestProcedure(sourceCode))).isEmpty();
            assertThat(sourceCode.popChar(emptyMap())).isEqualTo('n');
        }

        @Test
        void return_node_when_matches() {
            SourceCode sourceCode = givenSourceCode("(a");

            Optional<TestNode> testNode = nt("(").with(mandatory).parse(new TestProcedure(sourceCode));

            assertThat(testNode.get().getContent()).isEqualTo("a");
            assertThat(testNode.get().getPositionBegin()).isEqualTo(0 + START_POSITION);
            assertThat(sourceCode.hasCode()).isFalse();
        }

        @Test
        void trim_and_return_node_when_matches() {
            SourceCode sourceCode = givenSourceCode(" (a");

            Optional<TestNode> testNode = nt("(").with(mandatory).parse(new TestProcedure(sourceCode));

            assertThat(testNode.get().getContent()).isEqualTo("a");
            assertThat(testNode.get().getPositionBegin()).isEqualTo(1 + START_POSITION);
            assertThat(sourceCode.hasCode()).isFalse();
        }
    }

    @Nested
    class BeforeMandatory {
        NodeParser.Mandatory<TestNode, TestProcedure> mandatory =
                nt("a").<TestNode, TestProcedure>node(TestNode::new).mandatory("");


        @Test
        void return_empty_when_not_start_with() {
            SourceCode sourceCode = givenSourceCode("not match");

            assertThat(nt("s").before(mandatory).parse(new TestProcedure(sourceCode))).isEmpty();
            assertThat(sourceCode.popChar(emptyMap())).isEqualTo('n');
        }

        @Test
        void left_trim_even_not_match() {
            SourceCode sourceCode = givenSourceCode(" not match");

            assertThat(nt("s").before(mandatory).parse(new TestProcedure(sourceCode))).isEmpty();
            assertThat(sourceCode.popChar(emptyMap())).isEqualTo('n');
        }

        @Test
        void return_node_when_matches() {
            SourceCode sourceCode = givenSourceCode("(a");

            Optional<TestNode> testNode = nt("(").before(mandatory).parse(new TestProcedure(sourceCode));

            assertThat(testNode.get().getContent()).isEqualTo("a");
            assertThat(testNode.get().getPositionBegin()).isEqualTo(1 + START_POSITION);
            assertThat(sourceCode.hasCode()).isFalse();
        }

        @Test
        void trim_and_return_node_when_matches() {
            SourceCode sourceCode = givenSourceCode(" (a");

            Optional<TestNode> testNode = nt("(").before(mandatory).parse(new TestProcedure(sourceCode));

            assertThat(testNode.get().getContent()).isEqualTo("a");
            assertThat(testNode.get().getPositionBegin()).isEqualTo(2 + START_POSITION);
            assertThat(sourceCode.hasCode()).isFalse();
        }
    }

    @Nested
    class BeforeParser {
        NodeParser<TestNode, TestProcedure> parser = nt("a").node(TestNode::new);


        @Test
        void return_empty_when_not_start_with() {
            SourceCode sourceCode = givenSourceCode("not match");

            assertThat(nt("s").before(parser).parse(new TestProcedure(sourceCode))).isEmpty();
            assertThat(sourceCode.popChar(emptyMap())).isEqualTo('n');
        }

        @Test
        void left_trim_even_not_match() {
            SourceCode sourceCode = givenSourceCode(" not match");

            assertThat(nt("s").before(parser).parse(new TestProcedure(sourceCode))).isEmpty();
            assertThat(sourceCode.popChar(emptyMap())).isEqualTo(' ');
        }

        @Test
        void return_node_when_matches() {
            SourceCode sourceCode = givenSourceCode("(a");

            Optional<TestNode> testNode = nt("(").before(parser).parse(new TestProcedure(sourceCode));

            assertThat(testNode.get().getContent()).isEqualTo("a");
            assertThat(testNode.get().getPositionBegin()).isEqualTo(1 + START_POSITION);
            assertThat(sourceCode.hasCode()).isFalse();
        }

        @Test
        void trim_and_return_node_when_matches() {
            SourceCode sourceCode = givenSourceCode(" (a");

            Optional<TestNode> testNode = nt("(").before(parser).parse(new TestProcedure(sourceCode));

            assertThat(testNode.get().getContent()).isEqualTo("a");
            assertThat(testNode.get().getPositionBegin()).isEqualTo(2 + START_POSITION);
            assertThat(sourceCode.hasCode()).isFalse();
        }
    }
}