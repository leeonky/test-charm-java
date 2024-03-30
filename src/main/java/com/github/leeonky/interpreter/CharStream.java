package com.github.leeonky.interpreter;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CharStream {
    private final String code;
    int position = 0;

    public CharStream(String code) {
        this.code = code;
    }

    public int position() {
        return position;
    }

    public char current() {
        return code.charAt(position);
    }

    public boolean hasContent() {
        return position < code.length();
    }

    private boolean codeStartWith(Notation<?, ?, ?, ?, ?> notation) {
        trimBlank();
        return code.startsWith(notation.getLabel(), position);
    }

    public CharStream trimBlank() {
        while (hasContent() && Character.isWhitespace(current()))
            position++;
        return this;
    }

    public CharStream trimBlackAndComment(List<Notation<?, ?, ?, ?, ?>> comments) {
        while (comments.stream().anyMatch(this::codeStartWith)) {
            int newLinePosition = code.indexOf("\n", position);
            position = newLinePosition == -1 ? code.length() : newLinePosition + 1;
        }
        return trimBlank();
    }

    public int seek(int seek) {
        int position = this.position;
        this.position = this.position + seek;
        return position;
    }

    public char popChar() {
        return code.charAt(position++);
    }

    public boolean startsWith(String label) {
        return code.startsWith(label, position);
    }

    public boolean matches(TriplePredicate<String, Integer, Integer> endsWith, int length) {
        return endsWith.test(code, position, length);
    }

    public String contentUntil(String label) {
        int index = code.indexOf(label, position);
        return index >= 0 ? code.substring(position, index) : code.substring(position);
    }

    public <N> Optional<N> tryFetch(Supplier<Optional<N>> supplier) {
        int position = this.position;
        Optional<N> optionalNode = supplier.get();
        if (!optionalNode.isPresent())
            this.position = position;
        return optionalNode;
    }

    public String getCode() {
        return code;
    }

    public int lastIndexOf(String str, int position) {
        return code.lastIndexOf(str, position);
    }

    public int newlineBetween(int first, int second) {
        if (first < 0 || first >= second || second >= code.length())
            return -1;
        int position = rightScan(first, second - 1, CharStream::isBlank);
        if (position == -1)
            return -1;
        if (isNewline(code.charAt(position)))
            return rightScan(first, position, c -> isNewline(c) || isBlank(c));
        return -1;
    }

    private int rightScan(int rightToLeftEnd, int position, Predicate<Character> predicate) {
        while (predicate.test(code.charAt(position))) {
            if (--position < rightToLeftEnd)
                return -1;
        }
        return position;
    }

    private static boolean isBlank(char c) {
        return c == ' ' || c == '\b' || c == '\t';
    }

    private static boolean isNewline(char c) {
        return c == '\n' || c == '\r';
    }
}
