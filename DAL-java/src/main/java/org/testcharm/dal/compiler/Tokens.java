package org.testcharm.dal.compiler;

import org.testcharm.dal.ast.node.DALExpression;
import org.testcharm.dal.ast.node.DALNode;
import org.testcharm.dal.ast.opt.DALOperator;
import org.testcharm.dal.runtime.RuntimeContextBuilder;
import org.testcharm.interpreter.Token;
import org.testcharm.interpreter.TokenScanner;
import org.testcharm.util.NumberParser;

import java.util.List;

import static java.util.Collections.emptySet;
import static org.testcharm.dal.compiler.Constants.*;
import static org.testcharm.dal.compiler.Notations.Keywords;
import static org.testcharm.interpreter.TokenScanner.tokenScanner;
import static org.testcharm.interpreter.TokenSpec.tokenSpec;
import static org.testcharm.util.function.Extension.not;

public class Tokens {
    private static final NumberParser numberParser = new NumberParser();

    private static boolean isNumber(Token token) {
        return numberParser.parse(token.getContent()) != null;
    }

    private static boolean isPureInteger(Token token) {
        Number parse = numberParser.parse(token.getContent());
        return parse instanceof Integer;
    }

    public static final TokenScanner<RuntimeContextBuilder.DALRuntimeContext, DALNode, DALExpression, DALOperator, DALProcedure>
            NUMBER = tokenSpec(DIGITAL_OR_MINUS::contains, emptySet(), Tokens::notNumber).predicate(Tokens::isNumber).scanner(),
            PURE_INTEGER = tokenSpec(DIGITAL_OR_MINUS::contains, emptySet(), Tokens::notPureNumber)
                    .predicate(Tokens::isPureInteger).scanner(),
            SYMBOL = tokenSpec(not(PROPERTY_DELIMITER::contains), Keywords.ALL_STRING, PROPERTY_DELIMITER)
                    .predicate(not(Tokens::isNumber)).scanner(),
            USER_LITERAL_SYMBOL = tokenSpec(not(PROPERTY_DELIMITER::contains), Keywords.ALL_STRING, PROPERTY_DELIMITER)
                    .scanner(),
            DOT_SYMBOL = tokenSpec(not(PROPERTY_DELIMITER::contains), emptySet(), PROPERTY_DELIMITER)
                    .predicate(not(Tokens::isNumber)).scanner(),
            RELAX_SYMBOL = tokenSpec(not(RELAX_PROPERTY_DELIMITER::contains), Keywords.ALL_STRING, RELAX_PROPERTY_DELIMITER)
                    .predicate(not(Tokens::isNumber)).scanner(),
            RELAX_DOT_SYMBOL = tokenSpec(not(RELAX_PROPERTY_DELIMITER::contains), emptySet(), RELAX_PROPERTY_DELIMITER)
                    .predicate(not(Tokens::isNumber)).scanner(),
            SCHEMA = tokenSpec(not(DELIMITER::contains), Keywords.ALL_STRING, DELIMITER)
                    .predicate(not(Tokens::isNumber)).scanner(),
            CONSTANT = tokenSpec(c -> c.equals('$'), emptySet(), PROPERTY_DELIMITER)
                    .scanner();


    public static final TokenScanner.Mandatory<RuntimeContextBuilder.DALRuntimeContext, DALNode, DALExpression,
            DALOperator, DALProcedure>
            EXPRESSION_RELAX_STRING = relaxString(EXPRESSION_RELAX_STRING_TAIL),
            OBJECT_SCOPE_RELAX_STRING = relaxString(OBJECT_SCOPE_RELAX_STRING_TAIL),
            LIST_SCOPE_RELAX_STRING = relaxString(LIST_SCOPE_RELAX_STRING_TAIL),
            TABLE_CELL_RELAX_STRING = relaxString(TABLE_CELL_RELAX_STRING_TAIL),
            BRACKET_RELAX_STRING = relaxString(BRACKET_RELAX_STRING_TAIL);

    private static TokenScanner.Mandatory<RuntimeContextBuilder.DALRuntimeContext, DALNode, DALExpression, DALOperator,
            DALProcedure> relaxString(List<String> expressionRelaxStringTail) {
        return tokenScanner(false, (code, position, size) -> expressionRelaxStringTail.stream()
                .anyMatch(tail -> code.startsWith(tail, position)));
    }

    private static boolean notNumber(String code, int position, int size) {
        if (size == 0)
            return false;
        return notSymbolAfterPower(code, position) || notNumberPoint(code, position);
    }

    private static boolean notPureNumber(String code, int position, int size) {
        if (size == 0)
            return false;
        return notSymbolAfterPower(code, position);
    }

    private static boolean notNumberPoint(String code, int position) {
        return code.charAt(position) == '.' && (position == code.length() - 1
                || !DIGITAL.contains(code.charAt(position + 1)));
    }

    private static boolean notSymbolAfterPower(String code, int position) {
        char current = code.charAt(position);
        char lastChar = code.charAt(position - 1);
        return (DELIMITER.contains(current) && notSymbolAfterPower(lastChar, current))
                || (lastChar == '.' && !DIGITAL.contains(current));
    }

    private static boolean notSymbolAfterPower(Character lastChar, Character nextChar) {
        return (lastChar != 'e' && lastChar != 'E') || (nextChar != '-' && nextChar != '+');
    }
}
