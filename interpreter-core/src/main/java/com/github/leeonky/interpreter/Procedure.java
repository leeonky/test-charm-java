package com.github.leeonky.interpreter;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class Procedure<C extends RuntimeContext, N extends Node<C, N>, E extends Expression<C, N, E, O>,
        O extends Operator<C, N, O, E>> {

    private final SourceCode sourceCode;
    private final C runtimeContext;
    private final LinkedList<O> operators = new LinkedList<>();
    private final LinkedList<AtomicInteger> columns = new LinkedList<>();

    public Procedure(SourceCode sourceCode, C runtimeContext) {
        this.sourceCode = sourceCode;
        this.runtimeContext = runtimeContext;
    }

    public SourceCode getSourceCode() {
        return sourceCode;
    }

    public <T> T underOperator(O operator, Supplier<T> action) {
        operators.push(operator);
        try {
            return action.get();
        } finally {
            operators.pop();
        }
    }

    public <T> T positionOf(BiFunction<Integer, Integer, T> action) {
        return action.apply(sourceCode.nextPosition(), sourceCode.indent("\n"));
    }

    public <T> T withColumn(Supplier<T> action) {
        columns.push(new AtomicInteger());
        try {
            return action.get();
        } finally {
            columns.poll();
        }
    }

    public int getColumn() {
        return columns.getFirst().get();
    }

    public void incrementColumn() {
        columns.getFirst().incrementAndGet();
    }

    public abstract N createExpression(N node1, O operator, N node2);

    public C getRuntimeContext() {
        return runtimeContext;
    }

    public Optional<O> currentOperator() {
        return operators.stream().findFirst();
    }
}
