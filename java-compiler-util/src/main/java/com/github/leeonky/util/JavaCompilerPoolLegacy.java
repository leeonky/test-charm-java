package com.github.leeonky.util;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;

@Deprecated
public class JavaCompilerPoolLegacy {
    private final BlockingDeque<Integer> workspaces;
    private final String generatePackage;

    public JavaCompilerPoolLegacy(int maxCount, String generatePackage) {
        workspaces = IntStream.range(0, maxCount).boxed().collect(toCollection(LinkedBlockingDeque::new));
        this.generatePackage = generatePackage;
    }

    public JavaCompiler take() {
        return new JavaCompiler(generatePackage, Sneaky.get(workspaces::takeFirst));
    }

    public void giveBack(JavaCompiler compiler) {
        Sneaky.run(() -> workspaces.putLast(compiler.getId()));
    }
}
