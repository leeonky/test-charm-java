package com.github.leeonky.dal.extensions.basic;

import com.github.leeonky.dal.util.TextUtil;
import com.github.leeonky.interpreter.StringWithPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

public class Diff {
    private static final String RIGHT_ACTUAL = "Actual:";
    private final String detail;

    public Diff(String prefix, String expected, String actual) {
        detail = needDiff(expected, actual) ? makeDiffDetail(expected, actual, prefix) : "";
    }

    private boolean needDiff(String expected, String actual) {
        return actual != null && ((actual.contains("\n") || actual.contains("\r"))
                || expected.contains("\n") || expected.contains("\r"));
    }

    private String makeDiffDetail(String expected, String actual, String leftTitle) {
        int position = TextUtil.differentPosition(expected, Objects.requireNonNull(actual));
        int titleNewLine = leftTitle.lastIndexOf('\n');
        String title;
        if (titleNewLine == -1)
            title = "";
        else {
            title = leftTitle.substring(0, titleNewLine + 1);
            leftTitle = leftTitle.substring(titleNewLine + 1);
        }
        return new SideText(expected, position, leftTitle).merge(new SideText(actual, position, RIGHT_ACTUAL), title);
    }

    public String detail() {
        return detail;
    }

    private static class SideText {
        private final String title;
        private final List<String> lines;
        private final int width;

        public SideText(String content, int position, String title) {
            this.title = title;
            lines = new ArrayList<>(TextUtil.lines(new StringWithPosition(content).position(position).result()));
            width = Math.max(lines.stream().mapToInt(String::length).max().orElse(0), title.length());
        }

        private String merge(SideText right, String title) {
            StringBuilder builder = new StringBuilder().append(title);
            fillToSameLines(right);
            right.fillToSameLines(this);
            String leftFormat = "%-" + width + "s";
            builder.append(format(leftFormat, this.title)).append(" | ").append(right.title).append('\n');
            appendHeadMinus(builder);
            builder.append("-|-");
            right.appendHeadMinus(builder);
            for (int i = 0; i < lines.size(); i++)
                builder.append('\n').append(format(leftFormat, lines.get(i))).append(" | ").append(right.lines.get(i));
            return builder.toString().trim();
        }

        private void appendHeadMinus(StringBuilder builder) {
            for (int i = 0; i < width; i++)
                builder.append('-');
        }

        private void fillToSameLines(SideText another) {
            for (int i = lines.size(); i < another.lines.size(); i++)
                lines.add("");
        }
    }
}
