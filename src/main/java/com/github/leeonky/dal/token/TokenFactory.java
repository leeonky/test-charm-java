package com.github.leeonky.dal.token;

import com.github.leeonky.dal.Constants;
import com.github.leeonky.dal.parser.TokenParser;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

import static com.github.leeonky.dal.parser.NewTokenFactory.equalToCharacter;
import static com.github.leeonky.dal.parser.NewTokenFactory.startWith;
import static com.github.leeonky.dal.parser.SourceCodeMatcher.not;
import static com.github.leeonky.dal.parser.TokenContentInString.ALL_CHARACTERS;
import static com.github.leeonky.dal.parser.TokenContentInString.leftTrim;
import static com.github.leeonky.dal.parser.TokenContentInToken.byFactory;
import static com.github.leeonky.dal.parser.TokenParser.*;
import static com.github.leeonky.dal.parser.TokenStartEnd.before;
import static com.github.leeonky.dal.token.Token.*;

public interface TokenFactory {
    Function<String, Token> CONST_NUMBER_TOKEN = content ->
            getNumber(content).map(Token::constValueToken).orElse(null);
    Function<String, Token> OPERATOR_TOKEN = Token::operatorToken;
    Function<String, Token> PROPERTY_TOKEN = content -> {
        if (content.isEmpty())
            throw new IllegalTokenContentException("property chain not finished");
        return Token.propertyToken(content);
    };
    Function<String, Token> CONST_STRING_TOKEN = Token::constValueToken;
    Function<String, Token> REGEX_TOKEN = Token::regexToken;
    Function<String, Token> BEGIN_BRACKET_TOKEN = s -> Token.beginBracketToken();
    Function<String, Token> END_BRACKET_TOKEN = s -> Token.endBracketToken();
    Function<TokenStream, Token> BRACKET_PROPERTY_TOKEN = tokenStream -> {
        if (tokenStream.size() != 1)
            throw new IllegalTokenContentException("should given one property or array index in `[]`");
        return Token.propertyToken(tokenStream.pop().getPropertyOrIndex());
    };
    Function<String, Token> WORD_TOKEN = content -> {
        if (Constants.KeyWords.NULL.equals(content))
            return constValueToken(null);
        if (Constants.KeyWords.TRUE.equals(content))
            return constValueToken(true);
        if (Constants.KeyWords.FALSE.equals(content))
            return constValueToken(false);
        if (Constants.KeyWords.AND.equals(content))
            return operatorToken("&&");
        if (Constants.KeyWords.OR.equals(content))
            return operatorToken("||");
        if (Constants.KEYWORD_SETS.contains(content))
            return keyWordToken(content);
        return wordToken(content);
    };
    Function<TokenStream, Token> TOKEN_TREE = Token::treeToken;

    static TokenFactory createNumberTokenFactory() {
        return startWith(included(DIGITAL))
                .endWith(END_OF_CODE.or(before(DELIMITER)))
                .createAs(CONST_NUMBER_TOKEN);
    }

    static TokenFactory createBeanPropertyTokenFactory() {
        return startWith(excluded(CHARACTER('.')))
                .take(leftTrim(ALL_CHARACTERS))
                .endWith(END_OF_CODE.or(before(DELIMITER)).or(before(CHARACTER('.'))))
                .createAs(PROPERTY_TOKEN);
    }

    static TokenFactory createOperatorTokenFactory() {
        return startWith(included(OPERATOR.except(CHARACTER('/').when(AFTER_TOKEN_MATCHES))))
                .endWith(END_OF_CODE.or(before(not(OPERATOR))).or(before(CHARACTER('/').when(AFTER_OPERATOR_MATCHES))))
                .createAs(OPERATOR_TOKEN);
    }

    static TokenFactory createSingleQuotedStringTokenFactory() {
        return startWith(excluded(CHARACTER('\'')))
                .take(ALL_CHARACTERS
                        .escape("\\'", '\'')
                        .escape("\\\\", '\\'))
                .endWith(excluded(CHARACTER('\'')).orThrow("string should end with `'`"))
                .createAs(CONST_STRING_TOKEN);
    }

    static TokenFactory createDoubleQuotedStringTokenFactory() {
        return startWith(excluded(CHARACTER('"')))
                .take(ALL_CHARACTERS
                        .escape("\\\"", '"')
                        .escape("\\t", '\t')
                        .escape("\\n", '\n')
                        .escape("\\\\", '\\'))
                .endWith(excluded(CHARACTER('"')).orThrow("string should end with `\"`"))
                .createAs(CONST_STRING_TOKEN);
    }

    static TokenFactory createRegexTokenFactory() {
        return startWith(excluded(CHARACTER('/').when(AFTER_TOKEN_MATCHES)))
                .take(ALL_CHARACTERS
                        .escape("\\\\", '\\')
                        .escape("\\/", '/'))
                .endWith(excluded(CHARACTER('/')).orThrow("string should end with `/`"))
                .createAs(REGEX_TOKEN);
    }

    static TokenFactory createBeginBracketTokenFactory() {
        return equalToCharacter('(').createAs(BEGIN_BRACKET_TOKEN);
    }

    static TokenFactory createEndBracketTokenFactory() {
        return equalToCharacter(')').createAs(END_BRACKET_TOKEN);
    }

    static TokenFactory createBracketPropertyTokenFactory() {
        return startWith(excluded(CHARACTER('[').except(AFTER_TOKEN_MATCHES)))
                .take(byFactory(createNumberTokenFactory())
                        .or(createSingleQuotedStringTokenFactory())
                        .or(createDoubleQuotedStringTokenFactory()))
                .endWith(excluded(CHARACTER(']')).orThrow("should end with `]`"))
                .createAs(BRACKET_PROPERTY_TOKEN);
    }

    static TokenFactory createWordTokenFactory() {
        return startWith(included(ANY_CHARACTERS))
                .take(ALL_CHARACTERS)
                .endWith(END_OF_CODE.or(before(DELIMITER)))
                .createAs(WORD_TOKEN);
    }

    static Optional<Number> getNumber(String content) {
        try {
            return Optional.of(BigDecimal.valueOf(Long.decode(content)));
        } catch (NumberFormatException e) {
            try {
                return Optional.of(new BigDecimal(content));
            } catch (Exception exception) {
                return Optional.empty();
            }
        }
    }

    static TokenFactory createDALTokenFactory() {
        return startWith(BEGIN_OF_CODE)
                .take(byFactory(createBracketPropertyTokenFactory())
                        .or(createBeanPropertyTokenFactory())
                        .or(createNumberTokenFactory())
                        .or(createSingleQuotedStringTokenFactory())
                        .or(createDoubleQuotedStringTokenFactory())
                        .or(createRegexTokenFactory())
                        .or(createOperatorTokenFactory())
                        .or(createBeginBracketTokenFactory())
                        .or(createEndBracketTokenFactory())
                        .or(createWordTokenFactory()))
                .endWith(END_OF_CODE)
                .createAs(TOKEN_TREE);
    }

    static TokenFactory createPropertyChainFactory() {
        return startWith(BEGIN_OF_CODE)
                .take(byFactory(createBeanPropertyTokenFactory())
                        .or(createBracketPropertyTokenFactory()))
                .endWith(END_OF_CODE).createAs(TOKEN_TREE);
    }

    Token fetchToken(TokenParser parser);
}
