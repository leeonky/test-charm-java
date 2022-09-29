package com.github.leeonky.interpreter;

import java.util.Optional;
import java.util.function.Function;

public interface ClauseParser<C extends RuntimeContext<C>, N extends Node<C, N>,
        E extends Expression<C, N, E, O>, O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>>
        extends Parser<P, ClauseParser<C, N, E, O, P>,
        ClauseParser.Mandatory<C, N, E, O, P>, Clause<C, N>> {

    static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>, O extends
            Operator<C, N, O>, P extends Procedure<C, N, E, O, P>> ClauseParser<C, N, E, O, P> positionClause(
            ClauseParser<C, N, E, O, P> clauseParser) {
        return procedure -> procedure.positionOf(position -> clauseParser.parse(procedure)
                .map(clause -> node -> clause.expression(node).setPositionBegin(position)));
    }

    static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>, O extends
            Operator<C, N, O>, P extends Procedure<C, N, E, O, P>> ClauseParser.Mandatory<C, N, E, O, P> positionClause(
            ClauseParser.Mandatory<C, N, E, O, P> clauseMandatory) {
        return procedure -> procedure.positionOf(position -> {
            Clause<C, N> parse = clauseMandatory.parse(procedure);
            return node -> parse.expression(node).setPositionBegin(position);
        });
    }

    static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>, O extends
            Operator<C, N, O>, P extends Procedure<C, N, E, O, P>> ClauseParser.Mandatory<C, N, E, O, P> columnMandatory(
            Function<Integer, ClauseParser.Mandatory<C, N, E, O, P>> mandatoryFactory) {
        return procedure -> mandatoryFactory.apply(procedure.getColumn()).parse(procedure);
    }

    static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>, O extends
            Operator<C, N, O>, P extends Procedure<C, N, E, O, P>> ClauseParser<C, N, E, O, P> columnParser(
            Function<Integer, ClauseParser<C, N, E, O, P>> parserFactory) {
        return procedure -> parserFactory.apply(procedure.getColumn()).parse(procedure);
    }

    @Override
    default ClauseParser<C, N, E, O, P> castParser(Parser<P, ClauseParser<C, N, E, O, P>,
            Mandatory<C, N, E, O, P>, Clause<C, N>> parser) {
        return parser::parse;
    }

    @Override
    default Mandatory<C, N, E, O, P> castMandatory(Parser.Mandatory<P, ClauseParser<C, N, E, O, P>,
            Mandatory<C, N, E, O, P>, Clause<C, N>> mandatory) {
        return mandatory::parse;
    }

    default ClauseParser<C, N, E, O, P> concat(ClauseParser<C, N, E, O, P> clause) {
        return procedure -> parse(procedure).map(c1 -> clause.parse(procedure).<Clause<C, N>>map(c2 -> previous ->
                c2.expression(c1.expression(previous))).orElse(c1));
    }

    default Optional<N> parseAndMakeExpression(P procedure, N node) {
        return parse(procedure).map(clause -> clause.expression(node));
    }

    default N parseAndMakeExpressionOrInput(P procedure, N input) {
        return parseAndMakeExpression(procedure, input).orElse(input);
    }

    default N parseAndMakeExpressionOrInputContinuously(P procedure, N node) {
        N expression = parseAndMakeExpressionOrInput(procedure, node);
        if (expression == node)
            return expression;
        return parseAndMakeExpressionOrInputContinuously(procedure, expression);
    }

    interface Mandatory<C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>> extends Parser.Mandatory<P,
            ClauseParser<C, N, E, O, P>, Mandatory<C, N, E, O, P>, Clause<C, N>> {

        static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>, O extends
                Operator<C, N, O>, P extends Procedure<C, N, E, O, P>> Mandatory<C, N, E, O, P> clause(
                Function<N, NodeParser.Mandatory<C, N, E, O, P>> mandatoryFactory) {
            return procedure -> input -> mandatoryFactory.apply(input).parse(procedure);
        }

        @Override
        default ClauseParser<C, N, E, O, P> castParser(Parser<P, ClauseParser<C, N, E, O, P>,
                Mandatory<C, N, E, O, P>, Clause<C, N>> parser) {
            return parser::parse;
        }
    }
}
