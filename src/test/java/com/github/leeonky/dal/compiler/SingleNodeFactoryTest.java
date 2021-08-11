package com.github.leeonky.dal.compiler;

import com.github.leeonky.dal.ast.*;
import com.github.leeonky.dal.token.Token;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.leeonky.dal.compiler.NodeFactory.createConstNodeFactory;
import static com.github.leeonky.dal.compiler.NodeFactory.createPropertyNodeFactory;
import static com.github.leeonky.dal.token.Token.*;
import static org.assertj.core.api.Assertions.assertThat;

class SingleNodeFactoryTest {

    @Nested
    class FetchConstNode extends NodeFactoryTestBase {

        @Override
        protected NodeFactory getDefaultNodeFactory() {
            return createConstNodeFactory();
        }

        @Test
        void matches_and_return_node() {
            assertThat(fetchNodeWhenGivenToken(constValueToken("const string"), 10))
                    .isInstanceOf(ConstNode.class)
                    .hasFieldOrPropertyWithValue("value", "const string")
                    .hasFieldOrPropertyWithValue("positionBegin", 10);
        }

        @Test
        void return_null_when_dost_not_match() {
            assertThat(fetchNodeWhenGivenToken(operatorToken("+")))
                    .isNull();
        }
    }

    @Nested
    class FetchPropertyNode extends NodeFactoryTestBase {

        @Override
        protected NodeFactory getDefaultNodeFactory() {
            return createPropertyNodeFactory();
        }

        @Test
        void matches_and_return_node() {
            Node node = fetchNodeWhenGivenToken(propertyToken("name"), 10);

            assertThat(node)
                    .isInstanceOf(PropertyNode.class)
                    .hasFieldOrPropertyWithValue("name", "name")
                    .hasFieldOrPropertyWithValue("positionBegin", 10);
            assertThat(node.inspect()).isEqualTo(".name");
        }

        @Test
        void return_null_when_dost_not_match() {
            assertThat(fetchNodeWhenGivenToken(operatorToken("+")))
                    .isNull();
        }
    }

    @Nested
    class FetchRegexNode extends NodeFactoryTestBase {

        @Override
        protected NodeFactory getDefaultNodeFactory() {
            return NodeFactory.createRegexNodeFactory();
        }

        @Test
        void matches_and_return_node() {
            Node node = fetchNodeWhenGivenToken(regexToken("regex"), 10);

            assertThat(node)
                    .isInstanceOf(RegexNode.class)
                    .hasFieldOrPropertyWithValue("positionBegin", 10);
            assertThat(node.inspect()).isEqualTo("/regex/");
        }

        @Test
        void return_null_when_dost_not_match() {
            assertThat(fetchNodeWhenGivenToken(operatorToken("+")))
                    .isNull();
        }
    }

    @Nested
    class FetchBracketNode extends NodeFactoryTestBase {

        @Override
        protected NodeFactory getDefaultNodeFactory() {
            return NodeFactory.createBracketNodeFactory();
        }

        @Test
        void return_empty_when_not_matches() {
            assertThat(fetchNodeWhenGivenToken(constValueToken("not start with (")))
                    .isNull();
        }

        @Test
        void support_single_node_wrapped_with_bracket() {
            Node node = givenToken(beginBracketToken(), 10)
                    .givenToken(Token.constValueToken("str"))
                    .givenToken(Token.endBracketToken())
                    .fetchNode();

            assertThat(node).isInstanceOf(BracketNode.class)
                    .hasFieldOrPropertyWithValue("positionBegin", 10);

            assertThat(node.inspect()).isEqualTo("('str')");
        }

        @Test
        void support_expression_node_wrapped_with_bracket() {
            assertThat(givenCode("(1+1)").fetchNode().inspect()).isEqualTo("(1 + 1)");
        }

        @Test
        void raiser_error_when_bracket_has_no_data() {
            assertThat(invalidSyntaxToken(givenToken(beginBracketToken(), 100)))
                    .hasMessage("expect a value or expression")
                    .hasFieldOrPropertyWithValue("position", 100);
        }

        @Test
        void raiser_error_when_bracket_not_finished() {
            assertThat(invalidSyntaxToken(givenCode("(1")))
                    .hasMessage("missed end bracket")
                    .hasFieldOrPropertyWithValue("position", 2);
        }


        @Test
        void raiser_error_when_got_unexpected_token() {
            assertThat(invalidSyntaxToken(givenCode("(1 1")))
                    .hasMessage("unexpected token, ')' expected")
                    .hasFieldOrPropertyWithValue("position", 3);
        }
    }
}
