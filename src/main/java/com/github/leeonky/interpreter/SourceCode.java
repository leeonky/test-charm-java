package com.github.leeonky.interpreter;

import com.github.leeonky.interpreter.TokenScanner.Mandatory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.leeonky.interpreter.IfThenFactory.when;
import static com.github.leeonky.interpreter.TokenSpec.tokenSpec;

public class SourceCode {
    private final List<Notation> lineComments;
    private final CharStream charStream;
    private final int startPosition;

    public SourceCode(String code, List<Notation> lineComments) {
        charStream = new CharStream(code);
        this.lineComments = lineComments;
        trimBlankAndComment();
        startPosition = charStream.position();
    }

    @Deprecated
    public static <E extends Expression<C, N, E, O>, N extends Node<C, N>, C extends RuntimeContext<C>,
            O extends Operator<C, N, O>, S extends Procedure<C, N, E, O, S>> TokenScanner<C, N, E, O, S> tokenScanner(
            Predicate<Character> startsWith, Set<String> excluded, boolean trimStart, Set<Character> delimiters) {
        return tokenSpec(startsWith, excluded, delimiters).trimStart(trimStart).scanner();
    }

    @Deprecated
    public static <E extends Expression<C, N, E, O>, N extends Node<C, N>, C extends RuntimeContext<C>,
            O extends Operator<C, N, O>, S extends Procedure<C, N, E, O, S>> TokenScanner<C, N, E, O, S> tokenScanner(
            Predicate<Character> startsWith, Set<String> excluded, boolean trimStart, Set<Character> delimiters,
            Predicate<Token> validator) {
        return tokenSpec(startsWith, excluded, delimiters).trimStart(trimStart).predicate(validator).scanner();
    }

    @Deprecated
    public static <E extends Expression<C, N, E, O>, N extends Node<C, N>, C extends RuntimeContext<C>,
            O extends Operator<C, N, O>, S extends Procedure<C, N, E, O, S>> TokenScanner<C, N, E, O, S> tokenScanner(
            Predicate<Character> startsWith, Set<String> excluded, boolean trimStart,
            TriplePredicate<String, Integer, Integer> endsWith, Predicate<Token> predicate) {
        return tokenSpec(startsWith, excluded, endsWith).trimStart(trimStart).predicate(predicate).scanner();
    }

    public static <E extends Expression<C, N, E, O>, N extends Node<C, N>, C extends RuntimeContext<C>,
            O extends Operator<C, N, O>, S extends Procedure<C, N, E, O, S>> Mandatory<C, N, E, O, S> tokenScanner(
            boolean trimStart, TriplePredicate<String, Integer, Integer> endsWith) {
        return sourceCode -> {
            Token token = new Token(sourceCode.charStream.position);
            if (trimStart) {
                sourceCode.charStream.popChar();
                sourceCode.trimBlankAndComment();
            }
            int size = 0;
            while (sourceCode.charStream.hasContent() && !endsWith.test(sourceCode.charStream.code, sourceCode.charStream.position, size++))
                token.append(sourceCode.charStream.popChar());
            return token;
        };
    }

    @Deprecated
    public static SourceCode createSourceCode(String code, List<Notation> lineComments) {
        return new SourceCode(code, lineComments);
    }

    private SourceCode trimBlankAndComment() {
        charStream.trimBlackAndComment(lineComments);
        return this;
    }

    public boolean startsWith(Predicate<Character> predicate) {
        return trimBlankAndComment().charStream.hasContent() && predicate.test(charStream.current());
    }

    public boolean hasCode() {
        return charStream.hasContent();
    }

    public boolean startsWith(Notation notation) {
        trimBlankAndComment();
        return charStream.startsWith(notation.getLabel());
    }

    public boolean startsWith(String word) {
        return charStream.startsWith(word);
    }

    public char popChar(Map<String, Character> escapeChars) {
        return escapeChars.entrySet().stream().filter(e -> charStream.startsWith(e.getKey())).map(e -> {
            charStream.seek(e.getKey().length());
            return e.getValue();
        }).findFirst().orElseGet(charStream::popChar);
    }

    public boolean isBeginning() {
        return charStream.code.chars().skip(startPosition).limit(charStream.position - startPosition).allMatch(Character::isWhitespace);
    }

    public SyntaxException syntaxError(String message, int positionOffset) {
        return new SyntaxException(message, charStream.position + positionOffset);
    }

    public Optional<Token> popWord(Notation notation) {
        return popWord(notation, () -> true);
    }

    public Optional<Token> popWord(Notation notation, Supplier<Boolean> predicate) {
        return when(startsWith(notation) && predicate.get()).optional(() -> new Token(charStream.seek(notation.length()))
                .append(notation.getLabel()));
    }

    public <N> Optional<N> tryFetch(Supplier<Optional<N>> supplier) {
        int position = charStream.position;
        Optional<N> optionalNode = supplier.get();
        if (!optionalNode.isPresent())
            charStream.position = position;
        return optionalNode;
    }

    public boolean isEndOfLine() {
        if (!charStream.hasContent())
            return true;
        while (Character.isWhitespace(charStream.current()) && charStream.current() != '\n')
            charStream.popChar();
        return charStream.current() == '\n';
    }

    public String codeBefore(Notation notation) {
        int index = charStream.code.indexOf(notation.getLabel(), charStream.position);
        return index >= 0 ? charStream.code.substring(charStream.position, index) : charStream.code.substring(charStream.position);
    }

    public int nextPosition() {
        return trimBlankAndComment().charStream.position;
    }
}
