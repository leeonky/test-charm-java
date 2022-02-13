package com.github.leeonky.cucumber.restful;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Evaluator {
    public String eval(String expression) {
        return StringPattern.replaceAll(expression, "(\\$\\{[^}]*\\})", this::evalValue);
    }

    private String evalValue(String expression) {
        try {
            Class<?> extensionClass = Class.forName("com.github.leeonky.cucumber.restful.extensions.PathVariableReplacement");
            Method eval = extensionClass.getMethod("eval", String.class);
            return (String) eval.invoke(null, expression.substring(2, expression.length() - 1));
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ignored) {
            return expression;
        }
    }
}
