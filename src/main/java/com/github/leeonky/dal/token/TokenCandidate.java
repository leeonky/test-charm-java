package com.github.leeonky.dal.token;

import java.util.HashSet;
import java.util.Set;

public abstract class TokenCandidate {
    private final int startPosition;
    private final StringBuilder stringBuilder = new StringBuilder();
    private final Set<Character> split;
    private boolean finished = false;

    public TokenCandidate(SourceCode sourceCode) {
        this(sourceCode, new HashSet<>());
    }

    public TokenCandidate(SourceCode sourceCode, Set<Character> split) {
        this.split = split;
        startPosition = sourceCode.getPosition();
        char c = sourceCode.takeChar();
        if (!isDiscardBeginChar())
            stringBuilder.append(c);
    }

    protected void append(char c) {
        stringBuilder.append(c);
    }

    protected String content() {
        return stringBuilder.toString();
    }

    protected abstract Token toToken();

    protected boolean isDiscardBeginChar() {
        return false;
    }

    protected int getStartPosition() {
        return startPosition;
    }

    protected boolean isUnexpectedChar(char c) {
        return Character.isWhitespace(c) || c == '[' || split.contains(c);
    }

    Token getToken(SourceCode sourceCode) {
        while (sourceCode.hasContent()
                && !isDiscardedSuffix(sourceCode)
                && !isUnexpectedChar(sourceCode.getChar()))
            append(sourceCode.takeChar());
        return toToken();
    }

    protected String discardedSuffix() {
        return null;
    }

    protected boolean isFinished() {
        return finished;
    }

    private boolean isDiscardedSuffix(SourceCode sourceCode) {
        String suffix = discardedSuffix();
        if (suffix != null) {
            if (sourceCode.startsWith(suffix)) {
                sourceCode.seek(suffix.length());
                finished = true;
                return true;
            }
        }
        return false;
    }
}
