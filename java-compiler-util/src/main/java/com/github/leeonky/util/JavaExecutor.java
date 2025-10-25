package com.github.leeonky.util;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class JavaExecutor {
    private static final AtomicInteger index = new AtomicInteger();
    private static final ThreadLocal<JavaExecutor> localThreadJavaExecutor
            = ThreadLocal.withInitial(() -> new JavaExecutor(new JavaCompiler("src.test.generate.t", index.getAndIncrement())));

    private final Set<String> codes = new LinkedHashSet<>();

    public static JavaExecutor executor() {
        return localThreadJavaExecutor.get();
    }

    private final JavaCompiler javaCompiler;

    public JavaExecutor(JavaCompiler javaCompiler) {
        this.javaCompiler = javaCompiler;
    }

    private ExecutorTemplate executorTemplate = new ExecutorTemplate(this);

    public void addClass(String sourceCode) {
        codes.add(sourceCode);
    }

    public void setValueEvaluator(String expression) {
        executorTemplate.valueExpression(expression);
    }

    public Object evaluate() {
        codes.add(executorTemplate.asCode());
        javaCompiler.compile(codes);
        return executorTemplate.evaluate();
    }

    public Class<?> classOf(String className) {
        return javaCompiler.loadClass(className);
    }
}
