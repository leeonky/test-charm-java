package com.github.leeonky.dal.ast;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.interpreter.Operator;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.github.leeonky.dal.ast.AssertionFailure.assertUnexpectedFields;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public class ObjectNode extends DALNode {
    private final List<DALNode> expressions = new ArrayList<>();

    public ObjectNode(List<DALNode> expressions) {
        this.expressions.addAll(expressions);
    }

    public List<DALNode> getExpressions() {
        return expressions;
    }

    public ObjectNode() {
        this(Collections.emptyList());
    }

    @Override
    public String inspect() {
        return format("{%s}", expressions.stream().map(DALNode::inspect).collect(joining(", ")));
    }

    @Override
    public boolean judge(DALNode actualNode, Operator.Equal operator, RuntimeContextBuilder.DALRuntimeContext context) {
        Data data = actualNode.evaluateDataObject(context);
        checkNull(data);
        if (data.isList()) {
            AtomicInteger integer = new AtomicInteger(0);
            data.getListObjects().forEach(element -> assertUnexpectedFields(collectUnexpectedFields(element),
                    actualNode.inspect() + format("[%d]", integer.getAndIncrement()), operator.getPosition()));
        } else
            assertUnexpectedFields(collectUnexpectedFields(data), actualNode.inspect(), operator.getPosition());
        return judgeAll(context, data);
    }

    private void checkNull(Data data) {
        if (data.isNull())
            throw new AssertionFailure("The input value is null", getPositionBegin());
    }

    private Set<String> collectUnexpectedFields(Data data) {
        return new LinkedHashSet<String>(data.getFieldNames()) {{
            removeAll(expressions.stream().map(expression ->
                    data.firstFieldFromAlias(expression.getRootName()))
                    .collect(Collectors.toSet()));
        }};
    }

    @Override
    public boolean judge(DALNode actualNode, Operator.Matcher operator, RuntimeContextBuilder.DALRuntimeContext context) {
        Data data = actualNode.evaluateDataObject(context);
        checkNull(data);
        return judgeAll(context, data);
    }

    private boolean judgeAll(RuntimeContextBuilder.DALRuntimeContext context, Data data) {
        return context.newThisScope(data, () -> {
            expressions.forEach(expression -> expression.evaluate(context));
            return true;
        });
    }

    @Override
    public int getOperandPosition() {
        return expressions.size() > 0 ? expressions.get(expressions.size() - 1).getOperandPosition()
                : getPositionBegin();
    }
}
