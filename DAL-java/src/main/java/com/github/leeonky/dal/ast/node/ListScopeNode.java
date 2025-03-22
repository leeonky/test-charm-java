package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.ast.opt.Factory;
import com.github.leeonky.dal.compiler.Notations;
import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.interpreter.Clause;
import com.github.leeonky.interpreter.SyntaxException;
import com.github.leeonky.util.Zipped;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static com.github.leeonky.dal.ast.node.DALExpression.expression;
import static com.github.leeonky.dal.ast.node.InputNode.INPUT_NODE;
import static com.github.leeonky.dal.ast.node.SortGroupNode.NOP_COMPARATOR;
import static com.github.leeonky.dal.ast.node.SymbolNode.Type.BRACKET;
import static com.github.leeonky.dal.runtime.DalException.locateError;
import static com.github.leeonky.dal.runtime.ExpressionException.exception;
import static com.github.leeonky.dal.runtime.ExpressionException.opt1;
import static com.github.leeonky.util.Zipped.zip;
import static java.lang.String.format;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class ListScopeNode extends DALNode {
    private List<DALNode> verificationExpressions;
    private List<DALNode> inputExpressions;
    private List<Clause<DALNode>> inputClauses;
    private final Type type;
    private final Style style;
    private final Comparator<Data> comparator;

    public ListScopeNode(List<Clause<DALNode>> clauses, Comparator<Data> comparator, Style style) {
        type = guessType(clauses);
        inputClauses = clauses;
        this.comparator = comparator;
        this.style = style;
    }

    public ListScopeNode(List<DALNode> verificationExpressions, Type type, Comparator<Data> comparator, Style style) {
        this.verificationExpressions = inputExpressions = new ArrayList<>(verificationExpressions);
        this.type = type;
        this.comparator = comparator;
        this.style = style;
    }

    public ListScopeNode(List<Clause<DALNode>> clauses) {
        this(clauses, NOP_COMPARATOR, Style.LIST);
    }

    private List<DALNode> getVerificationExpressions(Data.DataList list, Data actual) {
        return verificationExpressions != null ? verificationExpressions : buildVerificationExpressions(list, actual)
                .stream().filter(node -> !(node instanceof ListEllipsisNode)).collect(toList());
    }

    private List<DALNode> buildVerificationExpressions(Data.DataList list, Data actual) {
        if (inputExpressions != null)
            return inputExpressions;
        return new ArrayList<DALNode>() {
            {
                List<Clause<DALNode>> usefulInputClauses = new ArrayList<>(inputClauses);
                if (type == Type.FIRST_N_ITEMS)
                    for (int i = 0; i < usefulInputClauses.size() - 1; i++)
                        add(buildIndexExpression(usefulInputClauses.get(i), i + list.firstIndex()));
                else if (type == Type.LAST_N_ITEMS)
                    for (int i = usefulInputClauses.size() - 1; i >= 0; i--)
                        add(0, buildIndexExpression(usefulInputClauses.get(i), i - usefulInputClauses.size()));
                else if (type == Type.ALL_ITEMS) {
                    Zipped<Clause<DALNode>, Integer> zipped = zip(usefulInputClauses, list.indexes());
                    zipped.forEachElement((clause, index) -> add(buildIndexExpression(clause, index)));
                    if (zipped.hasLeft())
                        throw differentSizeException(usefulInputClauses, actual, zipped.index());
                    if (zipped.hasRight() && !list.infinite())
                        throw differentSizeException(usefulInputClauses, actual, list.size());
                }
            }

            private DALNode buildIndexExpression(Clause<DALNode> clause, Integer index) {
                DALNode symbolNode = new SymbolNode(index, BRACKET);
                DALNode expression = clause.expression(expression(INPUT_NODE, Factory.executable(Notations.EMPTY), symbolNode));
                symbolNode.setPositionBegin(expression.getOperandPosition());
                return expression;
            }
        };
    }

    private AssertionFailure differentSizeException(List<Clause<DALNode>> usefulInputClauses, Data actual, int index) {
        String message = format("Unexpected list size\nExpected: <%d>\nActual: <%d>\nActual list: %s",
                usefulInputClauses.size(), index, actual.dump());
        return style == Style.ROW ? new DifferentCellSize(message, getPositionBegin())
                : new AssertionFailure(message, getPositionBegin());
    }

    @Override
    protected ExpectationFactory toVerify(DALRuntimeContext context) {
        return (operator, actual) -> new ExpectationFactory.Expectation() {
            @Override
            public Data matches() {
                return equalTo();
            }

            @Override
            public Data equalTo() {
                try {
                    Data sorted = actual.map(r -> opt1(r::list).sort(comparator)).resolve();
                    return sorted.execute(() -> type == Type.CONTAINS ?
                            verifyContainElement(context, sorted.resolved().list(), actual)
                            : verifyCorrespondingElement(context, getVerificationExpressions(sorted.resolved().list(), actual)));
                } catch (ListMappingElementAccessException e) {
                    throw exception(expression -> locateError(e, expression.left().getOperandPosition()));
                }
            }

            @Override
            public ExpectationFactory.Type type() {
                return ExpectationFactory.Type.LIST;
            }
        };
    }

    //    TODO tobe refactored
    private List<DALNode> buildVerificationExpressions() {
        if (inputExpressions != null)
            return inputExpressions;
        return new ArrayList<DALNode>() {{
            if (type == Type.LAST_N_ITEMS) {
                int negativeIndex = -1;
                for (int i = inputClauses.size() - 1; i >= 0; i--) {
                    add(0, inputClauses.get(i).expression(expression(INPUT_NODE, Factory.executable(Notations.EMPTY),
                            new SymbolNode(negativeIndex--, BRACKET))));
                }
            } else {
                for (int i = 0; i < inputClauses.size(); i++)
                    add(inputClauses.get(i).expression(expression(INPUT_NODE, Factory.executable(Notations.EMPTY),
                            new SymbolNode(i, BRACKET))));
            }
        }};
    }

    private Type guessType(List<Clause<DALNode>> clauses) {
        List<Boolean> isListEllipsis = clauses.stream().map(this::isListEllipsis).collect(toList());
        long ellipsesCount = isListEllipsis.stream().filter(Boolean::booleanValue).count();
        if (ellipsesCount > 0) {
            if (ellipsesCount == 1) {
                if (isListEllipsis.get(0))
                    return Type.LAST_N_ITEMS;
                if (isListEllipsis.get(isListEllipsis.size() - 1))
                    return Type.FIRST_N_ITEMS;
            } else if (ellipsesCount == 2 && isListEllipsis.get(0) && isListEllipsis.get(isListEllipsis.size() - 1))
                return Type.CONTAINS;
            throw new SyntaxException("Invalid ellipsis", clauses.get(isListEllipsis.lastIndexOf(true))
                    .expression(null).getOperandPosition());
        }
        return Type.ALL_ITEMS;
    }

    @Override
    public String inspect() {
        if (type == Type.CONTAINS)
            return inputClauses.stream().map(clause -> clause.expression(INPUT_NODE).inspect())
                    .collect(joining(", ", "[", "]"));
        return buildVerificationExpressions().stream().map(DALNode::inspect).collect(joining(", ", "[", "]"));
    }

    private Data verifyContainElement(DALRuntimeContext context, Data.DataList list, Data actual) {
        Iterator<Integer> iterator = list.indexes().iterator();
        List<Clause<DALNode>> expected = trimFirstEllipsis();
        for (int clauseIndex = 0; clauseIndex < expected.size(); clauseIndex++) {
            Clause<DALNode> clause = expected.get(clauseIndex);
            try {
                while (true) {
                    int elementIndex = getElementIndex(clause, iterator, actual);
                    try {
                        clause.expression(expression(INPUT_NODE, Factory.executable(Notations.EMPTY),
                                new SymbolNode(elementIndex, BRACKET))).evaluate(context);
                        break;
//                        TODO test should which exception ignore which exception not ignore
                    } catch (DalException ignore) {
                    }
                }
            } catch (AssertionFailure exception) {
                throw style == Style.LIST ? exception : new RowAssertionFailure(clauseIndex, exception);
            }
        }
        return actual;
    }

    private int getElementIndex(Clause<DALNode> clause, Iterator<Integer> iterator, Data actual) {
        if (iterator.hasNext())
            return iterator.next();
        throw new AssertionFailure("No such element in list: " + actual.dump(),
                clause.expression(INPUT_NODE).getOperandPosition());
    }

    private List<Clause<DALNode>> trimFirstEllipsis() {
        return inputClauses.subList(1, inputClauses.size() - 1);
    }

    private Data verifyCorrespondingElement(DALRuntimeContext context, List<DALNode> expressions) {
        Data result = context.data(null);
        if (style != Style.LIST)
            for (int index = 0; index < expressions.size(); index++)
                try {
                    result = expressions.get(index).evaluateData(context).resolve();
                } catch (DifferentCellSize differentCellSize) {
                    throw new RowAssertionFailure(index, differentCellSize);
                } catch (DalException dalException) {
                    if (style == Style.TABLE)
                        throw new ElementAssertionFailure(index, dalException);
                    throw dalException;
                }
        else {
            for (DALNode expression : expressions)
                result = expression.evaluateData(context).resolve();
        }
        return result;
    }

    private boolean isListEllipsis(Clause<DALNode> clause) {
        return clause.expression(INPUT_NODE) instanceof ListEllipsisNode;
    }

    public enum Type {
        ALL_ITEMS, FIRST_N_ITEMS, LAST_N_ITEMS, CONTAINS
    }

    public enum Style {
        LIST, TABLE, ROW
    }

    public static class NatureOrder extends ListScopeNode {

        @SuppressWarnings("unchecked")
        public NatureOrder(List<Clause<DALNode>> clauses) {
            super(clauses, Comparator.comparing(Data::instance, (Comparator) naturalOrder()), Style.LIST);
        }

        @Override
        public String inspect() {
            return "+" + super.inspect();
        }
    }

    public static class ReverseOrder extends ListScopeNode {

        @SuppressWarnings("unchecked")
        public ReverseOrder(List<Clause<DALNode>> clauses) {
            super(clauses, Comparator.comparing(Data::instance, (Comparator) reverseOrder()), Style.LIST);
        }

        @Override
        public String inspect() {
            return "-" + super.inspect();
        }
    }

    static class DifferentCellSize extends AssertionFailure {
        public DifferentCellSize(String format, int position) {
            super(format, position);
        }
    }
}
