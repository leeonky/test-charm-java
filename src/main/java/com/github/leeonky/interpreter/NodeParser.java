package com.github.leeonky.interpreter;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Optional.empty;

public interface NodeParser<C extends RuntimeContext<C>, N extends Node<C, N>,
        E extends Expression<C, N, E, O>, O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>>
        extends Parser<C, N, E, O, P, N> {
    static <E extends Expression<C, N, E, O>, N extends Node<C, N>, C extends RuntimeContext<C>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>> NodeParser<C, N, E, O, P> oneOf(
            NodeParser<C, N, E, O, P>... matchers) {
        return procedure -> Stream.of(matchers)
                .map(p -> p.parse(procedure)).filter(Optional::isPresent).findFirst().orElse(empty());
    }

    static <E extends Expression<C, N, E, O>, N extends Node<C, N>, C extends RuntimeContext<C>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>> NodeParser<C, N, E, O, P> lazy(
            Supplier<NodeParser<C, N, E, O, P>> parser) {
        return procedure -> parser.get().parse(procedure);
    }

    @Override
    Optional<N> parse(P procedure);

    default Mandatory<C, N, E, O, P> or(Mandatory<C, N, E, O, P> nodeFactory) {
        return procedure -> parse(procedure).orElseGet(() -> nodeFactory.parse(procedure));
    }

    default Mandatory<C, N, E, O, P> mandatory(String message) {
        return procedure -> parse(procedure).orElseThrow(() -> procedure.getSourceCode().syntaxError(message, 0));
    }

    //TODO rename
    default ClauseParser<C, N, E, O, P> castToClause() {
        return procedure -> parse(procedure).map(node -> p -> node);
    }

    default NodeParser<C, N, E, O, P> map(Function<N, N> mapping) {
        return procedure -> parse(procedure).map(mapping);
    }

    interface Mandatory<C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>> extends Parser.Mandatory<C, N, E, O, P, N> {

        @Override
        N parse(P procedure);

        @Deprecated
        default Mandatory<C, N, E, O, P> map(Function<N, N> mapping) {
            return procedure -> mapping.apply(parse(procedure));
        }

        default Mandatory<C, N, E, O, P> mandatoryNode(ClauseParser.Mandatory<C, N, E, O, P> expressionClauseMandatory) {
            return procedure -> {
                N node = parse(procedure);
                return expressionClauseMandatory.parse(procedure).makeExpression(node);
            };
        }

        default Mandatory<C, N, E, O, P> recursive(ClauseParser<C, N, E, O, P> clauseParser) {
            return procedure -> {
                N node = parse(procedure);
                Optional<Clause<C, N>> optionalNode = clauseParser.parse(procedure);
                while (optionalNode.isPresent()) {
                    node = optionalNode.get().makeExpression(node);
                    optionalNode = clauseParser.parse(procedure);
                }
                return node;
            };
        }
    }
}
