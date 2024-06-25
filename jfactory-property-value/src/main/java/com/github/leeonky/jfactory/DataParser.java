package com.github.leeonky.jfactory;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.interpreter.InterpreterException;
import com.github.leeonky.jfactory.helper.*;

import java.util.Map;

public class DataParser {
    private static final DAL DAL = new DALHelper().dal();

    public static PropertyValue data(String expression) {
        return new PropertyValue() {
            @Override
            public <T> Builder<T> setToBuilder(String property, Builder<T> builder) {
                Object data = parse(expression);
                if (property.equals(""))
                    return builder.properties(tryFlat(data));
                ObjectValue objectValue = new ObjectValue();
                objectValue.put(property, data);
                return builder.properties(objectValue.flat());
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static Map<String, ?> tryFlat(Object object) {
        if (object instanceof FlatAble)
            return ((FlatAble) object).flat();
        return (Map<String, ?>) object;
    }

    public static Object parse(String expression) {
        String prefix = guessPrefix(expression);
        ObjectReference objectReference = new ObjectReference();
        try {
            DAL.evaluateAll(objectReference, prefix + expression);
        } catch (InterpreterException e) {
            throw new IllegalArgumentException("\n" + e.show(prefix + expression, prefix.length()) + "\n\n" + e.getMessage());
        }
        return objectReference.value();
    }

    private static String guessPrefix(String expression) {
        String prefix = "";
        String trim = expression.trim();
        if (trim.startsWith("{") || trim.startsWith("|")
                || (trim.startsWith("[") && trim.endsWith("]")))
            prefix = ":";
        return prefix;
    }

    public static Specs specs(String expression) {
        String prefix = guessPrefix(expression);
        Specs specs = new Specs();
        try {
            DAL.evaluateAll(specs, prefix + expression);
        } catch (InterpreterException e) {
            throw new IllegalArgumentException("\n" + e.show(prefix + expression, prefix.length()) + "\n\n" + e.getMessage());
        }
        return specs;
    }

}
