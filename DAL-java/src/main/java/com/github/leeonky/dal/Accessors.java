package com.github.leeonky.dal;

import com.github.leeonky.interpreter.InterpreterException;

import java.util.function.Supplier;

public class Accessors {
    private final String expression;
    private static boolean dumpInput = true;
    private DAL dal;
    private static Supplier<DAL> dalFactory = () -> DAL.dal("AssertD");

    public static void setDALFactory(Supplier<DAL> dalFactory) {
        Accessors.dalFactory = dalFactory;
    }

    public static void dumpInput(boolean enable) {
        dumpInput = enable;
    }

    public Accessors by(DAL dal) {
        this.dal = dal;
        return this;
    }

    public Accessors(String expression) {
        this.expression = expression;
    }

    public static Accessors get(String expression) {
        return new Accessors(expression);
    }

    public <T> T from(Object input) {
        try {
            return getDAL().evaluate(input, expression);
        } catch (InterpreterException e) {
            String detailMessage = "\n" + e.show(expression, 0) + "\n\n" + e.getMessage();
            if (dumpInput)
                detailMessage += "\n\nThe root value was: "
                        + getDAL().getRuntimeContextBuilder().build(input).getThis().dump();
            throw new RuntimeException(detailMessage, e) {

                @Override
                public String toString() {
                    return RuntimeException.class.getName() + ":\n" + getMessage();
                }
            };
        }
    }

    private DAL getDAL() {
        if (dal == null)
            dal = dalFactory.get();
        return dal;
    }
}
