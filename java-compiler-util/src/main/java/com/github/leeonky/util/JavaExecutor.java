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

    private ExecutorMain executorMain = new ExecutorMain(this);

    public void addClass(String sourceCode) {
        String className = guessClassName(sourceCode);
        Optional<ClassDefinition> compiled = findDefinition(allCompiled, className);
        if (compiled.isPresent()) {
            if (!compiled.get().getCharContent(true).equals(sourceCode)) {
                unCompiled.add(sourceCode);
                allCompiled.remove(compiled.get());
                Sneaky.run(() -> Files.deleteIfExists(javaCompiler.getLocation().toPath().resolve(className.replace('.', '/') + ".class")));
            }
        } else {
            unCompiled.add(sourceCode);
        }
    }

    public ExecutorMain main() {
        return executorMain;
    }

    public Class<?> classOf(String className) {
        ClassLoader classLoader = URLClassLoader.newInstance(Sneaky.get(() ->
                new URL[]{javaCompiler.getLocation().getAbsoluteFile().toURI().toURL()}));

        Optional<ClassDefinition> compiled = findDefinition(allCompiled, className);
        if (!compiled.isPresent()) {
            if (!unCompiled.isEmpty()) {
                allCompiled.addAll(javaCompiler.compile(unCompiled));
                unCompiled.clear();
                compiled = findDefinition(allCompiled, className);
            }
        }

        Optional<? extends Class<?>> clazz = compiled.map(d -> Sneaky.get(() -> classLoader.loadClass(d.className())));
        return Sneaky.get(() -> clazz.orElseThrow(() -> new ClassNotFoundException(className)));
    }

    private Optional<ClassDefinition> findDefinition(Set<ClassDefinition> definitions, String className) {
        return definitions.stream().filter(a -> a.className().equals(className))
                .findFirst();
    }

    public void reset() {
        executorMain = new ExecutorMain(this);
        unCompiled.clear();
    }

    public JavaExecutor resetAll() {
        reset();
        unCompiled.clear();
        allCompiled.clear();
        return this;
    }
}
