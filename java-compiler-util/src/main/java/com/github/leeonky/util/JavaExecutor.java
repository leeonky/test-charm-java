package com.github.leeonky.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
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
        unCompiled.add(sourceCode);
    }

    public ExecutorMain main() {
        return context.executorMain;
    }

    public Class<?> classFor(String className) {
        return Sneaky.get(() -> allCompiled().stream().filter(a -> a.getSimpleClassName().equals(className))
                .map(d -> d.loadClass(context.classLoader))
                .findFirst().orElseThrow(() -> new ClassNotFoundException(className)));
    }

    private Set<Definition> allCompiled() {
        if (!unCompiled.isEmpty()) {
            allCompiled.addAll(javaCompiler.compile(unCompiled));
            unCompiled.clear();
        }
        return allCompiled;
    }

    class Context {
        private final ExecutorMain executorMain = new ExecutorMain(JavaExecutor.this);
        private final URLClassLoader classLoader = URLClassLoader.newInstance(Sneaky.get(() -> new URL[]{new File("").toURI().toURL()}));
    }
}
