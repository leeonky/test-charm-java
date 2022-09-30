package com.github.leeonky.interpreter;

import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

public interface NodeParser<N extends Node<?, N>, P extends Procedure<?, N, ?, ?>>
        extends Parser<P, NodeParser<N, P>, NodeParser.Mandatory<N, P>, N> {

    static <N extends Node<?, N>, P extends Procedure<?, N, ?, ?>> NodeParser.Mandatory<N, P> positionNode(
            NodeParser.Mandatory<N, P> mandatory) {
        return procedure -> procedure.positionOf(position -> mandatory.parse(procedure).setPositionBegin(position));
    }

    static <N extends Node<?, N>, P extends Procedure<?, N, ?, ?>> NodeParser<N, P> positionNode(
            NodeParser<N, P> mandatory) {
        return procedure -> procedure.positionOf(position -> mandatory.parse(procedure)
                .map(node -> node.setPositionBegin(position)));
    }

    static <N extends Node<?, N>, P extends Procedure<?, N, ?, ?>> NodeParser.Mandatory<N, P> columnMandatory(
            Function<Integer, NodeParser.Mandatory<N, P>> mandatoryFactory) {
        return procedure -> mandatoryFactory.apply(procedure.getColumn()).parse(procedure);
    }

    static <N extends Node<?, N>, P extends Procedure<?, N, ?, ?>> NodeParser<N, P> columnParser(
            Function<Integer, NodeParser<N, P>> parserFactory) {
        return procedure -> parserFactory.apply(procedure.getColumn()).parse(procedure);
    }

    @Override
    default NodeParser<N, P> castParser(Parser<P, NodeParser<N, P>,
            Mandatory<N, P>, N> parser) {
        return parser::parse;
    }

    @Override
    default Mandatory<N, P> castMandatory(Parser.Mandatory<P, NodeParser<N, P>,
            Mandatory<N, P>, N> mandatory) {
        return mandatory::parse;
    }

    default NodeParser<N, P> concat(ClauseParser.Mandatory<N, P> mandatory) {
        return procedure -> parse(procedure).map(node -> mandatory.parse(procedure).expression(node));
    }

    default NodeParser<N, P> concat(ClauseParser<N, P> clauseParser) {
        return procedure -> parse(procedure).map(node -> clauseParser.parseAndMakeExpressionOrInput(procedure, node));
    }

    default NodeParser<N, P> concatAll(ClauseParser<N, P> clauseParser) {
        return procedure -> parse(procedure).map(node ->
                clauseParser.parseAndMakeExpressionOrInputContinuously(procedure, node));
    }

    default NodeParser<N, P> with(ClauseParser<N, P> clauseParser) {
        return procedure -> procedure.getSourceCode().tryFetch(() -> parse(procedure)
                .flatMap(node -> clauseParser.parseAndMakeExpression(procedure, node)));
    }

    default NodeParser<N, P> with(ClauseParser.Mandatory<N, P> mandatory) {
        return procedure -> procedure.getSourceCode().tryFetch(() -> parse(procedure)
                .flatMap(node -> ofNullable(mandatory.parse(procedure).expression(node))));
    }

    default ClauseParser<N, P> clause(BiFunction<N, N, N> biFunction) {
        return procedure -> parse(procedure).map(n -> input -> biFunction.apply(input, n));
    }

    interface Mandatory<N extends Node<?, N>, P extends Procedure<?, N, ?, ?>> extends
            Parser.Mandatory<P, NodeParser<N, P>, Mandatory<N, P>, N> {

        @Override
        default NodeParser<N, P> castParser(Parser<P, NodeParser<N, P>,
                Mandatory<N, P>, N> parser) {
            return parser::parse;
        }

        @Override
        default Mandatory<N, P> castMandatory(Parser.Mandatory<P, NodeParser<N, P>, Mandatory<N, P>, N> mandatory) {
            return mandatory::parse;
        }

        default Mandatory<N, P> concat(ClauseParser.Mandatory<N, P> clauseMandatory) {
            return procedure -> {
                N node = parse(procedure);
                return clauseMandatory.parse(procedure).expression(node);
            };
        }

        default Mandatory<N, P> concat(ClauseParser<N, P> clauseParser) {
            return procedure -> clauseParser.parseAndMakeExpressionOrInput(procedure, parse(procedure));
        }

        default Mandatory<N, P> concatAll(ClauseParser<N, P> clauseParser) {
            return procedure -> clauseParser.parseAndMakeExpressionOrInputContinuously(procedure, parse(procedure));
        }

        default NodeParser<N, P> with(ClauseParser<N, P> clauseParser) {
            return procedure -> procedure.getSourceCode().tryFetch(() ->
                    clauseParser.parseAndMakeExpression(procedure, parse(procedure)));
        }

        default NodeParser.Mandatory<N, P> with(ClauseParser.Mandatory<N, P> mandatory) {
            return procedure -> {
                N input = parse(procedure);
                return mandatory.parse(procedure).expression(input);
            };
        }
    }
}
