package com.github.leeonky.cucumber.restful;

import com.github.leeonky.util.BeanClass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class Evaluator {
    public String eval(String expression) {
        return StringPattern.replaceAll(expression, "(\\$\\{[^}]*\\})", this::evalValue);
    }

    private String evalValue(String expression) {
        try {
            List<Class<?>> extensions = BeanClass.allTypesIn("com.github.leeonky.cucumber.restful.extensions");
            if (extensions.isEmpty()) {
                return expression;
            }
            Method eval = extensions.get(0).getMethod("eval", String.class);
            return (String) eval.invoke(null, expression.substring(2, expression.length() - 1));
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ignored) {
            return expression;
        }
    }

}
