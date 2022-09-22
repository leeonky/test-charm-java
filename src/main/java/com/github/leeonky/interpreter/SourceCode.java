package com.github.leeonky.interpreter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.leeonky.util.function.When.when;

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
        return charStream.position() == startPosition;
    }

    public SyntaxException syntaxError(String message, int positionOffset) {
        return new SyntaxException(message, charStream.position() + positionOffset);
    }

    public Optional<Token> popWord(Notation notation) {
        return popWord(notation, () -> true);
    }

    public Optional<Token> popWord(Notation notation, Supplier<Boolean> predicate) {
        return when(startsWith(notation) && predicate.get())
                .optional(() -> new Token(charStream.seek(notation.length())).append(notation.getLabel()));
    }

    public <N> Optional<N> tryFetch(Supplier<Optional<N>> supplier) {
        return charStream.tryFetch(supplier);
    }

    public boolean isEndOfLine() {
        if (!charStream.hasContent())
            return true;
        while (Character.isWhitespace(charStream.current()) && charStream.current() != '\n')
            charStream.popChar();
        return charStream.current() == '\n';
    }

    public String codeBefore(Notation notation) {
        return charStream.contentUntil(notation.getLabel());
    }

    public int nextPosition() {
        return trimBlankAndComment().charStream.position();
    }

    public Token fetchToken(boolean trimStart, TriplePredicate<String, Integer, Integer> endsWith) {
        Token token = new Token(charStream.position());
        if (trimStart) {
            charStream.popChar();
            trimBlankAndComment();
        }
        int size = 0;
        while (charStream.hasContent() && !charStream.matches(endsWith, size++))
            token.append(charStream.popChar());
        return token;
    }
}
