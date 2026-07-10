package org.testcharm.dal.compiler;

import org.testcharm.dal.ast.node.DALExpression;
import org.testcharm.dal.ast.node.DALNode;
import org.testcharm.dal.ast.opt.DALOperator;
import org.testcharm.dal.ast.opt.Factory;
import org.testcharm.dal.compiler.Notations.Keywords;
import org.testcharm.dal.runtime.Calculator;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.interpreter.OperatorParser;
import org.testcharm.interpreter.Procedure;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.testcharm.dal.ast.opt.Factory.ExpressionContextData.adapt;
import static org.testcharm.dal.ast.opt.Factory.*;
import static org.testcharm.dal.compiler.Constants.PROPERTY_DELIMITER_STRING;
import static org.testcharm.dal.compiler.Notations.COMMA;
import static org.testcharm.dal.compiler.Notations.Operators.*;
import static org.testcharm.interpreter.Parser.oneOf;
import static org.testcharm.util.function.Extension.not;

public class Operators {

    @GeneratedForJacocoIgnore
    private Operators() {
    }

    private static final OperatorParser<DALRuntimeContext, DALNode, DALOperator, DALProcedure, DALExpression>
            DEFAULT_OPERATOR = Procedure::currentOperator,
            MAYBE_PROPERTY_SLASH = SLASH.operator(() -> Factory.executable(SLASH));

    static final OperatorParser<DALRuntimeContext, DALNode, DALOperator, DALProcedure, DALExpression>
            IS = Notations.Operators.IS.keywordOperator(Factory::is, PROPERTY_DELIMITER_STRING),
            WHICH = Notations.Operators.WHICH.operator(Factory::which),
            PROPERTY_DOT = DOT.operator(() -> Factory.executable(DOT), not(DALProcedure::mayBeElementEllipsis)),
            PROPERTY_SLASH = procedure -> procedure.isEnableSlashProperty() ? MAYBE_PROPERTY_SLASH.parse(procedure) : empty(),
            PROPERTY_IMPLICIT = procedure -> of(Factory.executable(Notations.EMPTY)),
            PROPERTY_META = META.operator(() -> Factory.executable(META)),
            BINARY_ARITHMETIC_OPERATORS = oneOf(
                    AND.operator(() -> logical(AND, Calculator::and)),
                    OR.operator(() -> logical(OR, Calculator::or)),
                    Keywords.AND.keywordOperator(() -> logical(Keywords.AND, Calculator::and), PROPERTY_DELIMITER_STRING),
                    COMMA.operator(() -> logical(COMMA, Calculator::and), DALProcedure::isEnableCommaAnd),
                    NOT_EQUAL.operator(() -> comparator(NOT_EQUAL, adapt(Calculator::notEqual))),
                    Keywords.OR.keywordOperator(() -> logical(Keywords.OR, Calculator::or), PROPERTY_DELIMITER_STRING),
                    GREATER_OR_EQUAL.operator(() -> comparator(GREATER_OR_EQUAL, adapt(Calculator::greaterOrEqual))),
                    LESS_OR_EQUAL.operator(() -> comparator(LESS_OR_EQUAL, adapt(Calculator::lessOrEqual))),
                    GREATER.operator(() -> comparator(GREATER, adapt(Calculator::greater))),
                    LESS.operator(() -> comparator(LESS, adapt(Calculator::less)), not(DALProcedure::mayBeOpeningGroup)),
                    PLUS.operator(() -> plusSub(PLUS, Calculator::arithmetic, org.testcharm.dal.runtime.Operators.PLUS)),
                    SUBTRACTION.operator(() -> plusSub(SUBTRACTION, Calculator::arithmetic, org.testcharm.dal.runtime.Operators.SUB)),
                    MULTIPLICATION.operator(() -> mulDiv(MULTIPLICATION, Calculator::arithmetic, org.testcharm.dal.runtime.Operators.MUL)),
                    DIVISION.operator(() -> mulDiv(DIVISION, Calculator::arithmetic, org.testcharm.dal.runtime.Operators.DIV))),
            UNARY_OPERATORS = oneOf(MINUS.operator(() -> unary(MINUS, adapt(Calculator::negate)), not(DALProcedure::isCodeBeginning)),
                    PLUS.operator(() -> unary(PLUS, adapt(Calculator::positive)), not(DALProcedure::isCodeBeginning)),
                    NOT.operator(() -> unary(NOT, adapt(Calculator::not)), not(DALProcedure::mayBeUnEqual))),
            VERIFICATION_OPERATORS = oneOf(MATCHER.operator(Factory::match, not(DALProcedure::mayBeMetaProperty)),
                    EQUAL.operator(Factory::equal)),
            DATA_REMARK = Notations.Operators.DATA_REMARK.operator(Factory::dataRemark),
            CONST_REMARK = Notations.Operators.CONST_REMARK.operator(Factory::constRemark);

    static final OperatorParser.Mandatory<DALRuntimeContext, DALNode, DALOperator, DALProcedure, DALExpression>
            DEFAULT_VERIFICATION_OPERATOR = DEFAULT_OPERATOR.mandatory("");
}
