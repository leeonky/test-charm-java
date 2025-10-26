package com.github.leeonky.util;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class JavaExecutor {
    private static final AtomicInteger index = new AtomicInteger();
    private static final ThreadLocal<JavaExecutor> localThreadJavaExecutor
            = ThreadLocal.withInitial(() -> new JavaExecutor(new JavaCompiler("src.test.generate.t", index.getAndIncrement())));

    private final Set<String> unCompiled = new LinkedHashSet<>();
    private final Set<Definition> allCompiled = new LinkedHashSet<>();

    public static JavaExecutor executor() {
        return localThreadJavaExecutor.get();
    }

    private final JavaCompiler javaCompiler;

    public JavaExecutor(JavaCompiler javaCompiler) {
        this.javaCompiler = javaCompiler;
    }

    private Context context = new Context();

    public void addClass(String sourceCode) {
//        if (!findDefinition(allCompiled, guessClassName(sourceCode))
//                .map(d -> d.getMainCode().equals(sourceCode)).orElse(false))
        unCompiled.add(sourceCode);
    }

    public ExecutorMain main() {
        return context.executorMain;
    }

    public Class<?> classFor(String className, ClassLoader classLoader) {
        return Sneaky.get(() -> findDefinition(allCompiled(), className)
                .map(d -> d.loadClass(classLoader))
                .orElseThrow(() -> new ClassNotFoundException(className)));
    }

    private Optional<Definition> findDefinition(Set<Definition> definitions, String className) {
        return definitions.stream().filter(a -> a.getSimpleClassName().equals(className))
                .findFirst();
    }

    private Set<Definition> allCompiled() {
        if (!unCompiled.isEmpty()) {
            allCompiled.addAll(javaCompiler.compile(unCompiled));
            unCompiled.clear();
        }
        return allCompiled;
    }

    public void reset() {
        context = new Context();
        unCompiled.clear();
    }

    public JavaExecutor resetAll() {
        reset();
        unCompiled.clear();
        allCompiled.clear();
        return this;
    }

    class Context {
        private final ExecutorMain executorMain = new ExecutorMain(JavaExecutor.this);
    }
}
