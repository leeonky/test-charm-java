package com.github.leeonky.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StringNotation {
    private final String content;
    private final List<Integer> positions = new ArrayList<>();
    private final List<Integer> rows = new ArrayList<>();
    private final List<Integer> columns = new ArrayList<>();

    public StringNotation(String content) {
        this.content = content;
    }

    public StringNotation position(int position) {
        if (position >= 0 && position <= content.length())
            positions.add(position);
        return this;
    }

    public StringNotation row(int position) {
        if (position >= 0 && position <= content.length())
            rows.add(position);
        return this;
    }

    public String result() {
        StringBuilder result = new StringBuilder();
        SeparatedString separatedString = new SeparatedString(content, 0, 0);
        while (separatedString.printLine(result).hasNextLine()) {
            separatedString = separatedString.separatedNext().newLine(result);
        }
        return result.toString();
    }

    public String result(int offset) {
        StringBuilder result = new StringBuilder();
        SeparatedString separatedString = new SeparatedString(content.substring(offset), offset, offset);
        while (separatedString.printLine(result).hasNextLine()) {
            separatedString = separatedString.separatedNext().newLine(result);
        }
        return result.toString();
    }

    public StringNotation column(int position) {
        if (position >= 0 && position <= content.length())
            columns.add(position);
        return this;
    }
   
//    private static boolean isFullWidth(int c) {
//        return !('\u0000' <= c && c <= '\u00FF' || '\uFF61' <= c && c <= '\uFFDC' || '\uFFE8' <= c && c <= '\uFFEE');
//    }

    public class SeparatedString {
        private final int startPosition;
        private final int offset;
        private final String newLine;
        private final String[] lines;

        public SeparatedString(String content, int startPosition, int offset) {
            lines = content.split("\r\n|\n\r|\n|\r", 2);
            newLine = fetchNewLine(content);
            this.startPosition = startPosition;
            this.offset = offset;
        }

        private String fetchNewLine(String content) {
            return hasNextLine() ? content.substring(lines[0].length(), content.length() - lines[1].length()) : "\n";
        }

        public boolean hasNextLine() {
            return lines.length == 2;
        }

        private SeparatedString printLine(StringBuilder builder) {
            builder.append(lines[0]);
            printPositions(builder, positions);
            printPositions(builder, columns);
            printWholeLine(builder);
            return this;
        }

        private void printWholeLine(StringBuilder builder) {
            List<Integer> linePositions = linePosition(rows);
            if (!linePositions.isEmpty()) {
                builder.append(newLine);
                for (int i = 0; i <= lines[0].length(); i++)
                    builder.append('^');
            }
        }

        private void printPositions(StringBuilder builder, List<Integer> positions) {
            List<Integer> linePositions = linePosition(positions);
            if (!linePositions.isEmpty()) {
                int startEndIndex = startPosition - 1;
                builder.append(newLine);
                for (int position : linePositions) {
                    builder.append(String.format("%" + (position - startEndIndex) + "c", '^'));
                    startEndIndex = position;
                }
            }
        }

        private List<Integer> linePosition(List<Integer> positions) {
            return positions.stream().filter(i -> i >= startPosition && i <= startPosition + lines[0].length())
                    .sorted().collect(Collectors.toList());
        }

        private SeparatedString separatedNext() {
            return new SeparatedString(lines[1], startPosition + lines[0].length() + newLine.length(), offset);
        }

        private SeparatedString newLine(StringBuilder result) {
            result.append(newLine);
            return this;
        }
    }
}
