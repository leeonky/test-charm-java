package com.github.leeonky.interpreter;

import java.util.List;
import java.util.Optional;
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

    public int firstNonBlankBetween(int first, int second) {
        if (first < 0 || first >= second || second >= code.length())
            return -1;
        int position = second - 1;
        char c = code.charAt(position--);
        if (isBlank(c)) {
            while (position >= first && isBlank(code.charAt(position))) {
                position--;
            }
            return position;
        }
        return -1;
    }

    private boolean isBlank(char c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\b' || c == '\t';
    }
}
