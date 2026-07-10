package org.testcharm.dal;

import org.testcharm.interpreter.InterpreterException;

import java.util.function.Supplier;

public class Evaluator {
    private final String expression;
    private final Type type;
    private DAL dal;
    private static Supplier<DAL> dalFactory;
    private Object constants;
    private int codeOffset = 0;

    static {
        setDALFactory(DAL::dal);
    }

    public static void setDALFactory(Supplier<DAL> dalFactory) {
        Evaluator.dalFactory = dalFactory;
    }

    private Evaluator(String expression, Type type) {
        this.expression = expression;
        this.type = type;
    }

    public static Evaluator evaluate(String expression) {
        return new Evaluator(expression, Type.SINGLE);
    }

    public static Evaluator evaluateObject(String expression) {
        if (!expression.trim().startsWith(":") && (expression.trim().startsWith("{")
                || expression.trim().startsWith("[")
                || expression.trim().startsWith("|")))
            expression = ":\n" + expression;
        return new Evaluator(expression, Type.OBJECT);
    }

    public static Evaluator evaluateAll(String expressions) {
        return new Evaluator(expressions, Type.MULTIPLE);
    }

    public Evaluator by(DAL dal) {
        this.dal = dal;
        return this;
    }

    public Evaluator codeOffset(int codeOffset) {
        this.codeOffset = codeOffset;
        return this;
    }

    private DAL getDAL() {
        if (dal == null)
            dal = dalFactory.get();
        return dal;
    }

    public Evaluator constants(Object constants) {
        this.constants = constants;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T on(Object input) {
        try {
            if (type == Type.MULTIPLE)
                return (T) getDAL().evaluateAll(() -> input, expression, constants);
            else
                return getDAL().evaluate(() -> input, expression, null, constants);
        } catch (InterpreterException e) {
            String detailMessage = "\n" + e.show(expression, codeOffset) + "\n\n" + e.getMessage();
            throw new RuntimeException(detailMessage, e) {

                @Override
                public String toString() {
                    return RuntimeException.class.getName() + ":\n" + getMessage();
                }
            };
        }
    }

    enum Type {
        SINGLE, MULTIPLE, OBJECT
    }
}
