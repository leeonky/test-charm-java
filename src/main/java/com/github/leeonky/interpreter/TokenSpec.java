package com.github.leeonky.interpreter;

import java.util.Set;
import java.util.function.Predicate;

import static com.github.leeonky.interpreter.IfThenFactory.when;

public class TokenSpec {
    private final Predicate<Character> startsWith;
    private final Set<String> excluded;
    private final TriplePredicate<String, Integer, Integer> endsWith;
    private boolean trimStart = false;
    private Predicate<Token> predicate = token -> true;

    private TokenSpec(Predicate<Character> startsWith, Set<String> excluded,
                      TriplePredicate<String, Integer, Integer> endsWith) {
        this.startsWith = startsWith;
        this.excluded = excluded;
        this.endsWith = endsWith;
    }

    public static TokenSpec tokenSpec(Predicate<Character> startsWith, Set<String> excluded,
                                      TriplePredicate<String, Integer, Integer> endsWith) {
        return new TokenSpec(startsWith, excluded, endsWith);
    }

    public static TokenSpec tokenSpec(Predicate<Character> startsWith, Set<String> excluded, Set<Character> delimiters) {
        return tokenSpec(startsWith, excluded, (code, position, size) -> delimiters.contains(code.charAt(position)));
    }

    public TokenSpec trimStart(boolean f) {
        trimStart = f;
        return this;
    }

    public TokenSpec predicate(Predicate<Token> predicate) {
        this.predicate = predicate;
        return this;
    }

    public <E extends Expression<C, N, E, O>, N extends Node<C, N>, C extends RuntimeContext<C>,
            O extends Operator<C, N, O>, S extends Procedure<C, N, E, O, S>> TokenScanner<C, N, E, O, S> scanner() {
        return sourceCode -> sourceCode.tryFetch(() -> when(sourceCode.startsWith(startsWith)).optional(() -> {
            Token token = TokenScanner.tokenScanner(trimStart, endsWith).scan(sourceCode);
            return !excluded.contains(token.getContent()) && predicate.test(token) ? token : null;
        }));
    }
}
