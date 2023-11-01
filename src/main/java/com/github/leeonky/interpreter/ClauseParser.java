package com.github.leeonky.interpreter;

import java.util.Optional;
import java.util.function.Function;

public interface ClauseParser<N extends Node<?, N>, P extends Procedure<?, N, ?, ?>>
        extends Parser<P, ClauseParser<N, P>, ClauseParser.Mandatory<N, P>, Clause<N>> {

    static <N extends Node<?, N>, P extends Procedure<?, N, ?, ?>> ClauseParser<N, P> positionClause(
            ClauseParser<N, P> clauseParser) {
        return procedure -> procedure.positionOf((position, indent) -> clauseParser.parse(procedure)
                .map(clause -> node -> clause.expression(node).setPositionBegin(position).setIndent(indent)));
    }

    static <N extends Node<?, N>, P extends Procedure<?, N, ?, ?>> ClauseParser.Mandatory<N, P> positionClause(
            ClauseParser.Mandatory<N, P> clauseMandatory) {
        return procedure -> procedure.positionOf((position, indent) -> {
            Clause<N> parse = clauseMandatory.parse(procedure);
            return node -> parse.expression(node).setPositionBegin(position).setIndent(indent);
        });
    }

    static <N extends Node<?, N>, P extends Procedure<?, N, ?, ?>> ClauseParser.Mandatory<N, P> columnMandatory(
            Function<Integer, ClauseParser.Mandatory<N, P>> mandatoryFactory) {
        return procedure -> mandatoryFactory.apply(procedure.getColumn()).parse(procedure);
    }

    static <N extends Node<?, N>, P extends Procedure<?, N, ?, ?>> ClauseParser<N, P> columnParser(
            Function<Integer, ClauseParser<N, P>> parserFactory) {
        return procedure -> parserFactory.apply(procedure.getColumn()).parse(procedure);
    }

    @Override
    default ClauseParser<N, P> castParser(Parser<P, ClauseParser<N, P>,
            Mandatory<N, P>, Clause<N>> parser) {
        return parser::parse;
    }

    @Override
    default Mandatory<N, P> castMandatory(Parser.Mandatory<P, ClauseParser<N, P>, Mandatory<N, P>,
            Clause<N>> mandatory) {
        return mandatory::parse;
    }

    default ClauseParser<N, P> concat(ClauseParser<N, P> clause) {
        return procedure -> parse(procedure).map(c1 -> clause.parse(procedure).<Clause<N>>map(c2 -> previous ->
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

    default ClauseParser<N, P> concatAll(ClauseParser<N, P> clauseParser) {
        return procedure -> parse(procedure).map(clause -> input ->
                clauseParser.parseAndMakeExpressionOrInputContinuously(procedure, clause.expression(input)));
    }

    interface Mandatory<N extends Node<?, N>, P extends Procedure<?, N, ?, ?>> extends
            Parser.Mandatory<P, ClauseParser<N, P>, Mandatory<N, P>, Clause<N>> {

        static <N extends Node<?, N>, P extends Procedure<?, N, ?, ?>> Mandatory<N, P> clause(
                Function<N, NodeParser.Mandatory<N, P>> mandatoryFactory) {
            return procedure -> input -> mandatoryFactory.apply(input).parse(procedure);
        }

        @Override
        default ClauseParser<N, P> castParser(Parser<P, ClauseParser<N, P>,
                Mandatory<N, P>, Clause<N>> parser) {
            return parser::parse;
        }
    }
}
