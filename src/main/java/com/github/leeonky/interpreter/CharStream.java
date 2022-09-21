package com.github.leeonky.interpreter;

import java.util.List;

public class CharStream {
    String code;
    int startPosition;
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

    private boolean codeStartWith(Notation notation) {
        while (hasContent() && Character.isWhitespace(current()))
            position++;
        return code.startsWith(notation.getLabel(), position);
    }

    public void trimBlackAndComment(List<Notation> comments) {
        while (comments.stream().anyMatch(this::codeStartWith)) {
            int newLinePosition = code.indexOf("\n", position);
            position = newLinePosition == -1 ? code.length() : newLinePosition + 1;
        }
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
}
