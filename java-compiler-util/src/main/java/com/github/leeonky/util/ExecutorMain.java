package com.github.leeonky.util;

import java.util.function.Supplier;

public class ExecutorMain {
    private final JavaExecutor javaExecutor;
    private String returnExpression = "null";
    private static final String CLASS_NAME = "Executor";

    public ExecutorMain(JavaExecutor javaExecutor) {
        this.javaExecutor = javaExecutor;
    }

    public void returnExpression(String expression) {
        returnExpression = expression;
    }

    public String asCode() {
        return new StringBuilder()
                .append("import java.util.function.Supplier;\n")
                .append("import com.github.leeonky.jfactory.JFactory;\n")
                .append("public class " + CLASS_NAME + " implements Supplier {\n")
                .append("public Object get() {")
                .append("return ").append(returnExpression).append(";\n")
                .append("}\n")
                .append("}").toString();
    }

    public Object evaluate() {
        javaExecutor.addClass(asCode());
        return ((Supplier<?>) (Sneaky.get(() -> javaExecutor.classFor(CLASS_NAME).newInstance()))).get();
    }
}
