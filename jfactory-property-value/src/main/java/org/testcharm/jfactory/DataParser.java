package org.testcharm.jfactory;

import org.testcharm.dal.DAL;
import org.testcharm.interpreter.InterpreterException;
import org.testcharm.jfactory.helper.*;

import java.util.Map;

public class DataParser {
    private static final ThreadLocal<DAL> instance = new ThreadLocal<>();

    public static PropertyValue data(String expression) {
        return new PropertyValue() {
            @Override
            public <T> Builder<T> applyToBuilder(String property, Builder<T> builder) {
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
            dal().evaluateAll(objectReference, prefix + expression);
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
            dal().evaluateAll(specs, prefix + expression);
        } catch (InterpreterException e) {
            throw new IllegalArgumentException("\n" + e.show(prefix + expression, prefix.length()) + "\n\n" + e.getMessage());
        }
        return specs;
    }

    private static DAL dal() {
        if (instance.get() == null)
            instance.set(new DALHelper().dal());
        return instance.get();
    }
}
