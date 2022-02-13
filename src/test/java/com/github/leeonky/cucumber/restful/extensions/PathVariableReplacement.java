package com.github.leeonky.cucumber.restful.extensions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class PathVariableReplacement {

    public static Map<String, String> replacements = new HashMap<>();

    public static UnaryOperator<String> evaluator = s -> {
        throw new IllegalArgumentException();
    };

    private static String noReplacement(String s) {
        throw new IllegalArgumentException();
    }

    public static String eval(String expression) {
        return evaluator.apply(expression);
    }

    public static void reset() {
        evaluator = PathVariableReplacement::noReplacement;
        replacements.clear();
    }
}
