package com.github.leeonky.dal.ast;

import com.github.leeonky.dal.runtime.DalException;
import com.github.leeonky.dal.runtime.ElementAssertionFailure;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.github.leeonky.dal.ast.HeaderNode.bySequence;
import static com.github.leeonky.dal.ast.RowNode.printTableRow;
import static com.github.leeonky.dal.runtime.FunctionUtil.transpose;
import static com.github.leeonky.dal.runtime.FunctionUtil.zip;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class TableNode extends Node {
    private final List<HeaderNode> headers;
    private final List<RowNode> rows;
    private final Type type;
    private final boolean hasRowIndex;

    public TableNode(List<HeaderNode> headers, List<RowNode> row) {
        this(headers, row, Type.NORMAL);
    }

    public TableNode(List<HeaderNode> headers, List<RowNode> row, Type type) {
        this.headers = new ArrayList<>(headers);
        rows = new ArrayList<>(row);
        this.type = type;
        hasRowIndex = (!row.isEmpty()) && row.get(0).hasIndex();
    }

    public List<HeaderNode> getHeaders() {
        return headers;
    }

    public List<RowNode> getRows() {
        return rows;
    }

    @Override
    public String inspect() {
        return type.inspect(headers, rows);
    }

    @Override
    public boolean judge(Node actualNode, Operator.Equal operator, RuntimeContextBuilder.RuntimeContext context) {
        return judgeRows(actualNode, operator, context);
    }

    @Override
    public boolean judge(Node actualNode, Operator.Matcher operator, RuntimeContextBuilder.RuntimeContext context) {
        return judgeRows(actualNode, operator, context);
    }

    private boolean judgeRows(Node actualNode, Operator operator, RuntimeContextBuilder.RuntimeContext context) {
        try {
            if (hasRowIndex) {
                return new ListNode(rows.stream().map(rowNode -> rowNode.toExpressionClause(operator)
                        .makeExpression(null)).collect(toList()), true, ListNode.Type.FIRST_N_ITEMS)
                        .judgeAll(context, actualNode.evaluateDataObject(context).setListComparator(collectComparator(context)));
            }
            return new ListNode(rows.stream().map(rowNode -> rowNode.toExpressionClause(operator)).collect(toList()), true)
                    .judgeAll(context, actualNode.evaluateDataObject(context).setListComparator(collectComparator(context)));
        } catch (ElementAssertionFailure elementAssertionFailure) {
            throw type.toDalException(elementAssertionFailure, this);
        }
    }

    private Comparator<Object> collectComparator(RuntimeContextBuilder.RuntimeContext context) {
        return headers.stream().sorted(bySequence())
                .map(headerNode -> headerNode.getListComparator(context))
                .reduce(Comparator::thenComparing)
                .orElse(SequenceNode.NOP_COMPARATOR);
    }

    public enum Type {
        NORMAL {
            @Override
            protected DalException toDalException(ElementAssertionFailure elementAssertionFailure, TableNode tableNode) {
                return elementAssertionFailure.linePositionException();
            }

            @Override
            protected String inspect(List<HeaderNode> headers, List<RowNode> rows) {
                return String.join("\n", new ArrayList<String>() {{
                    add(printTableRow(headers.stream().map(HeaderNode::inspect)));
                    rows.stream().map(RowNode::inspect).forEach(this::add);
                }});
            }
        }, TRANSPOSED {
            @Override
            protected DalException toDalException(ElementAssertionFailure elementAssertionFailure, TableNode tableNode) {
                return elementAssertionFailure.columnPositionException(tableNode);
            }

            @Override
            protected String inspect(List<HeaderNode> headers, List<RowNode> rows) {
                String tableContent = zip(headers.stream().map(HeaderNode::inspect).collect(toList()).stream(),
                        inspectCells(rows, headers.size()).stream(), this::mergeHeaderAndCells)
                        .map(RowNode::printTableRow).collect(joining("\n"));
                return rows.stream().anyMatch(RowNode::hasSchemaOrOperator) ?
                        String.format("| >> %s\n%s", printTableRow(rows.stream().map(rowNode ->
                                rowNode.inspectSchemaAndOperator().trim())), tableContent) : ">>" + tableContent;
            }

            private ArrayList<String> mergeHeaderAndCells(String h, List<String> cells) {
                return new ArrayList<String>() {{
                    add(h);
                    addAll(cells);
                }};
            }

            private List<List<String>> inspectCells(List<RowNode> rows, int headerCount) {
                List<List<String>> rowCells = transpose(rows.stream().map(RowNode::inspectCells).collect(toList()));
                return rowCells.isEmpty() ? Collections.nCopies(headerCount, emptyList()) : rowCells;
            }
        };

        protected abstract DalException toDalException(ElementAssertionFailure elementAssertionFailure, TableNode tableNode);

        protected abstract String inspect(List<HeaderNode> headers, List<RowNode> rows);
    }
}
