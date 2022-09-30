package com.github.leeonky.interpreter;

import java.util.Optional;
import java.util.function.Function;

public interface TokenScanner<C extends RuntimeContext, N extends Node<C, N>, E extends Expression<C, N, E, O>,
        O extends Operator<C, N, O>, P extends Procedure<C, N, E, O>> {
    static <E extends Expression<C, N, E, O>, N extends Node<C, N>, C extends RuntimeContext,
            O extends Operator<C, N, O>, S extends Procedure<C, N, E, O>> Mandatory<C, N, E, O, S> tokenScanner(
            boolean trimStart, TriplePredicate<String, Integer, Integer> endsWith) {
        return sourceCode -> sourceCode.fetchToken(trimStart, endsWith);
    }

    Optional<Token> scan(SourceCode sourceCode);

    default NodeParser<N, P> nodeParser(Function<Token, N> mapper) {
        return procedure -> scan(procedure.getSourceCode()).map(token ->
                mapper.apply(token).setPositionBegin(token.getPosition()));
    }

    interface Mandatory<C extends RuntimeContext, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O>> {
        Token scan(SourceCode sourceCode);

        default NodeParser.Mandatory<N, P> nodeParser(Function<Token, N> mapper) {
            return procedure -> {
                Token token = scan(procedure.getSourceCode());
                return mapper.apply(token).setPositionBegin(token.getPosition());
            };
        }
    }
}
