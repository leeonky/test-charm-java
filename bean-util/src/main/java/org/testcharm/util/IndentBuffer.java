package org.testcharm.util;

import static java.lang.String.join;
import static java.util.Collections.nCopies;

public class IndentBuffer {
    private final int indent;
    private final Content content;
    private StringBuilder pending;
    private int length;

    private IndentBuffer(int indent, StringBuilder pending, Content content) {
        this.indent = indent;
        this.content = content;
        this.pending = pending;
        length = content.length();
    }

    public static IndentBuffer create(int maxLineCount) {
        return new IndentBuffer(0, new StringBuilder(), new Content(maxLineCount));
    }

    public IndentBuffer append(String s) {
        length = content.append(takePending(), s).length();
        return this;
    }

    public IndentBuffer defer(String then) {
        pending.append(then);
        return this;
    }

    public IndentBuffer newLine() {
        return defer("\n" + join("", nCopies(indent, "    ")));
    }

    public IndentBuffer optionalNewLine() {
        if (length != content.length())
            newLine();
        return this;
    }

    public IndentBuffer indent() {
        return createSub(1);
    }

    public IndentBuffer fork() {
        return createSub(0);
    }

    private IndentBuffer createSub(int extraIndent) {
        return new IndentBuffer(indent + extraIndent, takePending(), content);
    }

    private StringBuilder takePending() {
        StringBuilder temp = pending;
        pending = new StringBuilder();
        return temp;
    }

    public String content() {
        return content.toString();
    }

    public int length() {
        return content.length();
    }

    static class Content {
        private final StringBuilder stringBuilder = new StringBuilder();
        private final int maxLineCount;
        private int lineCount = 0;
        private boolean finished = false;

        Content(int maxLineCount) {
            this.maxLineCount = maxLineCount;
        }

        int length() {
            return stringBuilder.length();
        }

        Content append(StringBuilder pending, String text) {
            if (!finished) {
                if (pending.length() != 0) {
                    if ((lineCount += pending.chars().filter(c -> c == '\n').count()) >= maxLineCount) {
                        stringBuilder.append("\n...");
                        finished = true;
                        return this;
                    }
                    stringBuilder.append(pending);
                }
                stringBuilder.append(text);
            }
            return this;
        }

        @Override
        public String toString() {
            return stringBuilder.toString();
        }
    }
}
