package com.github.leeonky.dal.token;

public abstract class TextParser {
    private final StringBuffer content = new StringBuffer();
    private boolean isEscape = false;
    private boolean isFinished = false;

    protected abstract String escape(char c);

    public boolean feed(char c) {
        if (isFinished())
            throw new IllegalArgumentException("content is finished!");
        if (isEscape) {
            content.append(escape(c));
            isEscape = false;
        } else {
            if (isEscapeChar(c))
                isEscape = true;
            else {
                isFinished = isFinishedChar(c);
                content.append(c);
            }
        }
        return !isFinished();
    }

    protected abstract boolean isFinishedChar(char c);

    protected abstract boolean isEscapeChar(char c);

    public boolean isFinished() {
        return isFinished;
    }

    public String value() {
        if (!isFinished())
            throw new IllegalStateException("content is finished");
        return getContent(content);
    }

    protected abstract String getContent(StringBuffer stringBuffer);
}
