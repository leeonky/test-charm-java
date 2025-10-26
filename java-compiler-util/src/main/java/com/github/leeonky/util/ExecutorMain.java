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
    private final Set<String> registers = new LinkedHashSet<>();
    private final Map<String, Object> declarationValues = new HashMap<>();
    private Executor executor = null;

    public ExecutorMain(JavaExecutor javaExecutor) {
        this.javaExecutor = javaExecutor;
    }

    private String asCode() {
        StringBuilder builder = new StringBuilder()
                .append("import com.github.leeonky.jfactory.*;\n")
                .append("import com.github.leeonky.util.*;\n")
                .append("import java.util.*;\n")
                .append("public class ").append(CLASS_NAME)
                .append(" implements com.github.leeonky.util.ExecutorMain.Executor {\n");
        for (String declaration : declarations)
            builder.append("public ").append(declaration).append(";\n");
        builder.append("public void register").append("() {\n");
        for (String register : registers)
            builder.append(register.replaceAll(";+$", "")).append(";\n");
        builder.append("}\n")
                .append("public Object execute() {")
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
        this.registers.add(registers);
        executor = null;
    }

    public void returnExpression(String expression) {
        expression = expression.replaceAll(";+$", "");
        if (!Objects.equals(returnExpression, expression)) {
            returnExpression = expression;
            executor = null;
        }
    }

    public interface Executor {
        void register();

        Object execute();
    }
}
