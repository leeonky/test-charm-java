package com.github.leeonky.interpreter;

public class Token {
    private final StringBuilder content;
    private final int position;

    public int getPosition() {
        return position;
    }

    public Token(int position) {
        this.position = position;
        content = new StringBuilder();
    }

    public String getContent() {
        return content.toString();
    }

    public void append(char c) {
        content.append(c);
    }

    public Token append(String str) {
        content.append(str);
        return this;
    }
}
