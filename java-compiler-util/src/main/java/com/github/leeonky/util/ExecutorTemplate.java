package com.github.leeonky.util;

import java.util.function.Supplier;

public class ExecutorTemplate {
    private final JavaExecutor javaExecutor;
    private String valueExpression = "null";
    private static final String CLASS_NAME = "Executor";

    public ExecutorTemplate(JavaExecutor javaExecutor) {
        this.javaExecutor = javaExecutor;
    }

    public ExecutorTemplate valueExpression(String expression) {
        valueExpression = expression;
        return this;
    }

    public String asCode() {
        StringBuilder source = new StringBuilder();
        source.append("import java.util.function.Supplier;\n");
        source.append("import com.github.leeonky.jfactory.JFactory;\n");
        source.append("public class " + CLASS_NAME + " implements Supplier {\n")
                .append("public Object get() {")
                .append("return ").append(valueExpression)
                .append(";\n}\n")
                .append("}");
        return source.toString();
    }

    public Object evaluate() {
        return ((Supplier<?>) (Sneaky.get(() -> javaExecutor.classOf(CLASS_NAME).newInstance()))).get();
    }
}
