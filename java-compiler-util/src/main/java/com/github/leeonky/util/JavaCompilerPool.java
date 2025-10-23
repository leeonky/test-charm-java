package com.github.leeonky.util;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;

public class JavaCompilerPool {
    private final BlockingDeque<Integer> workspaces;
    private final String generatePackage;

    public JavaCompilerPool(int maxCount, String generatePackage) {
        workspaces = IntStream.range(0, maxCount).boxed().collect(toCollection(LinkedBlockingDeque::new));
        this.generatePackage = generatePackage;
    }

    public JavaCompilerLegacy take() {
        return new JavaCompilerLegacy(generatePackage, Sneaky.get(workspaces::takeFirst));
    }

    public void giveBack(JavaCompilerLegacy compiler) {
        Sneaky.run(() -> workspaces.putLast(compiler.getId()));
    }
}
