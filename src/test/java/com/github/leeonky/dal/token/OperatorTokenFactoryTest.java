package com.github.leeonky.dal.token;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.github.leeonky.dal.token.Token.operatorToken;
import static org.assertj.core.api.Assertions.assertThat;

class OperatorTokenFactoryTest {

    private TokenFactory createTokenFactory() {
        return TokenFactory.createOperatorTokenFactory();
    }

    private void shouldParse(String code, String value) {
        assertThat(parseToken(code)).isEqualTo(operatorToken(value));
    }

    private Token parseToken(String s) {
        return createTokenFactory().fetchToken(new SourceCode(s), null);
    }

    @Nested
    class CodeMatches {

        @Test
        void return_empty_when_no_code() {
            assertThat(parseToken("")).isNull();
        }

        @Test
        void return_empty_when_first_char_is_not_digital() {
            assertThat(parseToken("not start with operator char")).isNull();
        }

        @Test
        void should_return_empty_when_last_token_is_operator_matches() {
            assertThat(createTokenFactory().fetchToken(new SourceCode("/"), Token.operatorToken("~")))
                    .isNull();

            assertThat(createTokenFactory().fetchToken(new SourceCode("+"), Token.operatorToken("~")))
                    .isEqualTo(Token.operatorToken("+"));

            assertThat(createTokenFactory().fetchToken(new SourceCode("/"), null))
                    .isEqualTo(Token.operatorToken("/"));
        }

    }

    @Nested
    class HasDelimiter {

        @Test
        void should_return_token_when_given_valid_code() {
            shouldParse("+ ", "+");
        }

        @Test
        void distinguish_regex_after_operator_matches() {
            shouldParse("~/ ", "~");
        }

        @ParameterizedTest
        @ValueSource(strings = {"-", "!", "=", ">", "<", "+", "*", "/", "~", ">=", "<=", "!=", "&&", "||"})
        void finish_parse_and_source_code_seek_back_to_delimiter(String opt) {
            TokenFactory tokenFactory = createTokenFactory();
            SourceCode sourceCode = new SourceCode(opt + "a");
            assertThat(tokenFactory.fetchToken(sourceCode, null))
                    .isEqualTo(operatorToken(opt));
            assertThat(sourceCode.currentChar()).isEqualTo('a');
        }
    }

    @Nested
    class NoDelimiter {

        @ParameterizedTest
        @ValueSource(strings = {"-", "!", "=", ">", "<", "+", "*", "/", "~", ">=", "<=", "!=", "&&", "||"})
        void allow_get_value_when_parser_not_finished(String opt) {
            shouldParse(opt, opt);
        }
    }
}
