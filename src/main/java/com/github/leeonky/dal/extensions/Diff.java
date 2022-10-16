package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.util.TextUtil;
import com.github.leeonky.interpreter.StringWithPosition;

import java.util.List;

public class Diff {
    private static final String LEFT_EXPECT = "Expect:";
    private static final String RIGHT_ACTUAL = "Actual:";
    private final List<String> expectedInfo;
    private final List<String> actualInfo;
    private final int leftWidth;
    private final int rightWidth;
    private final StringBuilder builder = new StringBuilder().append("Diff:");
    private final boolean hasDiff;
    private final String leftFormat;

    public Diff(String expected, String actual) {
        hasDiff = expected.contains("\n") || expected.contains("\r");
        int position = TextUtil.differentPosition(expected, actual);
        expectedInfo = TextUtil.lines(new StringWithPosition(expected).position(position).result());
        actualInfo = TextUtil.lines(new StringWithPosition(actual).position(position).result());
        leftWidth = Math.max(expectedInfo.stream().mapToInt(String::length).max().orElse(0), LEFT_EXPECT.length());
        rightWidth = Math.max(actualInfo.stream().mapToInt(String::length).max().orElse(0), RIGHT_ACTUAL.length());
        leftFormat = "\n%-" + leftWidth + "s";
    }

    public String detail() {
        if (hasDiff) {
            printLine(LEFT_EXPECT, RIGHT_ACTUAL);
           
            builder.append('\n');
            for (int i = 0; i < leftWidth; i++)
                builder.append('-');
            builder.append("-|-");
            for (int i = 0; i < rightWidth; i++)
                builder.append('-');

            for (int i = 0; i < 3; i++)
                printLine(expectedInfo.get(i), actualInfo.get(i));
            return builder.toString();
        }
        return "";
    }

    private void printLine(String left, String right) {
        builder.append(String.format(leftFormat, left)).append(" | ").append(right);
    }
}
