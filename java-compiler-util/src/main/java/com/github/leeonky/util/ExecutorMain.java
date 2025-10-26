package com.github.leeonky.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import static java.util.Collections.addAll;

public class ExecutorMain {
    private final JavaExecutor javaExecutor;
    private String returnExpression = "null";
    private static final String CLASS_NAME = "Executor";
    private final Set<String> declarations = new LinkedHashSet<>();
    private final List<String> registers = new ArrayList<>();
    private final Set<String> dependencies = new LinkedHashSet<>();
    private final Map<String, Object> declarationValues = new HashMap<>();
    private Executor executor = null;

    public ExecutorMain(JavaExecutor javaExecutor) {
        this.javaExecutor = javaExecutor;
    }

    private String asCode() {
        StringBuilder builder = new StringBuilder();
        for (String dependency : dependencies)
            builder.append("import ").append(dependency).append(";\n");
        builder.append("public class ").append(CLASS_NAME)
                .append(" implements com.github.leeonky.util.ExecutorMain.Executor {\n");
        for (String declaration : declarations)
            builder.append("public ").append(declaration).append(";\n");
        builder.append("public void register").append("() {\n");
        for (String register : registers)
            builder.append(register.replaceAll(";+$", "")).append(";\n");
        builder.append("}\n")
                .append("public Object execute() {\n")
                .append("return ").append(returnExpression).append(";\n")
                .append("}\n")
                .append("}");
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public Object evaluate() {
        if (executor == null) {
            javaExecutor.addClass(asCode());
            BeanClass<Executor> executorType = (BeanClass<Executor>) BeanClass.create(javaExecutor.classFor(CLASS_NAME,
                    URLClassLoader.newInstance(Sneaky.get(() -> new URL[]{new File("").toURI().toURL()}))));
            executor = executorType.newInstance();
            declarationValues.forEach((key, value) ->
                    executorType.getPropertyWriter(key).setValue(executor, value));
            executorType.getPropertyReaders().forEach(((s, propertyReader) ->
                    declarationValues.put(s, propertyReader.getValue(executor))));
            executor.register();
        }
        return executor.execute();
    }

    public void addDeclarations(String declarations) {
        addAll(this.declarations, declarations.split(";"));
        executor = null;
    }

    public void addRegisters(String registers) {
        if (executor != null) {
            executor = null;
            this.registers.clear();
        }
        this.registers.add(registers);
    }

    public void importDependency(String... dependencies) {
        addAll(this.dependencies, dependencies);
        executor = null;
    }

    public ExecutorMain returnExpression(String expression) {
        expression = expression.replaceAll(";+$", "");
        if (!Objects.equals(returnExpression, expression)) {
            returnExpression = expression;
            executor = null;
        }

        return this;
    }

    public interface Executor {
        void register();

        Object execute();
    }
}
