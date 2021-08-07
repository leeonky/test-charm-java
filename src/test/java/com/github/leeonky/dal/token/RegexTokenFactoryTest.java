package com.github.leeonky.dal.token;

import com.github.leeonky.dal.DALCompiler;
import com.github.leeonky.dal.parser.ParsingContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RegexTokenFactoryTest extends TokenFactoryTestBase {
    private static final Token OPT_MATCHES = Token.operatorToken(DALCompiler.MATCHES);

    @Override
    protected Token previousToken() {
        return OPT_MATCHES;
    }

    @Override
    protected TokenFactory createTokenFactory() {
        return TokenFactory.createRegexTokenFactory();
    }

    @Override
    protected Token createToken(String value) {
        return Token.regexToken(value);
    }

    @Nested
    class CodeMatches {

        @Test
        void return_empty_when_first_char_is_not_digital() {
            assertThat(parseToken("not start with /")).isNull();
        }

        @Test
        void return_empty_when_previous_token_is_not_opt_matches() {
            assertThat(createTokenFactory().fetchToken(new ParsingContext(new SourceCode("/hello/"), null)))
                    .isNull();
        }

        @Test
        void should_return_regex_token_when_given_valid_code() {
            assertThat(createTokenFactory().fetchToken(new ParsingContext(new SourceCode("/hello/"), OPT_MATCHES)))
                    .isEqualTo(Token.regexToken("hello"));
        }
    }

    @Nested
    class Parse {

        @Test
        void should_not_contains_quotations() {
            shouldParse("//", "");
            shouldParse("/[0-9]/", "[0-9]");
        }

        @Test
        void escape_char() {
            shouldParse("/\\\\/", "\\");
            shouldParse("/\\//", "/");
        }

        @Test
        void keep_origin_when_not_supported_escape_char() {
            shouldParse("/\\h/", "\\h");
        }
    }

    @Nested
    class HasDelimiter {

        @Test
        void should_return_token_when_given_valid_code() {
            shouldParse("//", "");
            shouldParse("/[0-9]/", "[0-9]");
        }

        @Test
        void seek_to_right_position_after_fetch_token() {
            SourceCode sourceCode = new SourceCode("/hello/=");

            createTokenFactory().fetchToken(new ParsingContext(sourceCode, OPT_MATCHES));

            assertThat(sourceCode.currentChar()).isEqualTo('=');
        }
    }

    @Nested
    class NoDelimiter {

        @Test
        void allow_get_value_when_parser_not_finished() {
            assertThat(invalidSyntaxCode("/no end"))
                    .hasMessage("string should end with `/`")
                    .hasFieldOrPropertyWithValue("position", 7);

            assertThat(invalidSyntaxCode("/escape is not complete\\"))
                    .hasMessage("string should end with `/`")
                    .hasFieldOrPropertyWithValue("position", 24);
        }
    }
}
