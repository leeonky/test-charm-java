package com.github.leeonky.interpreter;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class StringNotation {
    private final String content;
    private final List<Integer> positions = new ArrayList<>();
    private final List<Integer> rows = new ArrayList<>();

    public StringNotation(String content) {
        this.content = content;
    }

    public StringNotation position(int position) {
        if (position >= 0 && position < content.length())
            positions.add(position);
        return this;
    }

    public String result() {
        int startPosition = -1;
        String newLine = "\n";
        String content = this.content;
        StringBuilder result = new StringBuilder();
        while (true) {
            String[] lines = content.split("\r\n|\n\r|\n|\r", 2);
            result.append(lines[0])
                    .append(markLine(startPosition, startPosition + lines[0].length(), newLine))
                    .append(markRow(startPosition, startPosition + lines[0].length(), newLine))
            ;
            if (lines.length > 1) {
                newLine = content.substring(lines[0].length(), content.length() - lines[1].length());
                startPosition += lines[0].length() + newLine.length();
                content = lines[1];
                result.append(newLine);
            } else
                return result.toString();
        }
    }

    private String markRow(int startPosition, int endPosition, String newLine) {
        List<Integer> rowPositions = rows.stream()
                .filter(i -> i <= endPosition && i > startPosition).collect(toList());
        if (rowPositions.isEmpty())
            return "";
        StringBuilder builder = new StringBuilder().append(newLine);
        for (int i = startPosition; i < endPosition; i++) {
            builder.append('^');
        }
        return builder.toString();
    }

    private String markLine(int startPosition, int endPosition, String newLine) {
        List<Integer> linePositions = positions.stream()
                .filter(i -> i <= endPosition && i > startPosition).collect(toList());
        if (linePositions.isEmpty())
            return "";
        StringBuilder builder = new StringBuilder().append(newLine);
        int position = startPosition;
        for (Integer linePosition : linePositions) {
            builder.append(String.format("%" + (linePosition - position) + "c", '^'));
            position = linePosition;
        }
        return builder.toString();
    }

    public StringNotation row(int position) {
        if (position >= 0 && position < content.length())
            rows.add(position);
        return this;
    }
}
