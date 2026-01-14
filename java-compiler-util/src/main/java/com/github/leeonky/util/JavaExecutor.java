package com.github.leeonky.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.leeonky.util.ClassDefinition.guessClassName;

public class JavaExecutor {
    private static final AtomicInteger index = new AtomicInteger();
    private static final ThreadLocal<JavaExecutor> localThreadJavaExecutor
            = ThreadLocal.withInitial(() -> new JavaExecutor(new JavaCompiler("src.test.generate.t", index.getAndIncrement())));

    private final Set<String> unCompiled = new LinkedHashSet<>();
    private final Set<ClassDefinition> allCompiled = new LinkedHashSet<>();

    public static JavaExecutor executor() {
        return localThreadJavaExecutor.get();
    }

    private final JavaCompiler javaCompiler;

    public JavaExecutor(JavaCompiler javaCompiler) {
        this.javaCompiler = javaCompiler;
    }

    private Context context = new Context();

    public void addClass(String sourceCode) {
        String className = guessClassName(sourceCode);
        if (!findDefinition(allCompiled, className)
                .map(d -> d.getCharContent(true).equals(sourceCode)).orElse(false)) {
            Sneaky.run(() -> Files.deleteIfExists(javaCompiler.getLocation().toPath().resolve(className.replace('.', '/') + ".class")));
            unCompiled.add(sourceCode);
        }
    }

    public ExecutorMain main() {
        return context.executorMain;
    }

    public Class<?> classOf(String className) {
        ClassLoader classLoader = URLClassLoader.newInstance(Sneaky.get(() ->
                new URL[]{javaCompiler.getLocation().getAbsoluteFile().toURI().toURL()}));
        return Sneaky.get(() -> findDefinition(allCompiled(), className)
                .map(d -> Sneaky.get(() -> classLoader.loadClass(d.className())))
                .orElseThrow(() -> new ClassNotFoundException(className)));
    }

    private Optional<ClassDefinition> findDefinition(Set<ClassDefinition> definitions, String className) {
        return definitions.stream().filter(a -> a.className().equals(className))
                .findFirst();
    }

    private Set<ClassDefinition> allCompiled() {
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
