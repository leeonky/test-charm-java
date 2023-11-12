package com.github.leeonky.jfactory;

import com.github.leeonky.util.function.TriFunction;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import static java.lang.String.format;

@Deprecated
public class TablePropertyValue implements PropertyValue {
    private final String table;

    private TablePropertyValue(String table) {
        this.table = table;
    }

    static <T> BinaryOperator<T> notAllowParallelReduce() {
        return (o1, o2) -> {
            throw new IllegalStateException("Not allow parallel here!");
        };
    }

    static <T, R> R reduceWithIndex(Stream<T> stream, R input, TriFunction<Integer, R, T, R> triFunction) {
        AtomicInteger index = new AtomicInteger(0);
        return stream.reduce(input, (reducer, line) -> triFunction.apply(index.getAndIncrement(), reducer, line),
                notAllowParallelReduce());
    }

    public static PropertyValue table(String table) {
        return new TablePropertyValue(table);
    }

    @Override
    public <T> Builder<T> setToBuilder(String property, Builder<T> builder) {
        String[] lines = table.split(System.lineSeparator());
        String[] headers = getCells(lines[0]);
        return reduceWithIndex(Stream.of(lines).skip(1), builder, (rowIndex, rowReduceBuilder, row) -> {
            String[] cells = getCells(row);
            String traitAndSpec = getTraitAndSpec(row);
            if (cells.length != headers.length)
                throw new IllegalArgumentException("Invalid table at row: " + rowIndex + ", different size of cells and headers.");
            return reduceWithIndex(Stream.of(headers), rowReduceBuilder, (columnIndex, columnReduceBuilder, cell) ->
                    columnReduceBuilder.property(format("%s[%d]%s.%s", property, rowIndex, traitAndSpec,
                            headers[columnIndex]), cells[columnIndex]));
        });
    }

    private String getTraitAndSpec(String row) {
        String traitAndSpec = row.split("\\|")[0].trim();
        return traitAndSpec.isEmpty() ? "" : "(" + traitAndSpec + ")";
    }

    private String[] getCells(String line) {
        return Stream.of(line.split("\\|")).skip(1).map(String::trim).toArray(String[]::new);
    }
}
