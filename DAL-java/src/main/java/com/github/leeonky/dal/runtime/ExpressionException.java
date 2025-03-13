package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.ast.node.DALExpression;
import com.github.leeonky.interpreter.InterpreterException;

import java.util.function.Function;
import java.util.function.Supplier;

//TODO check all exception
public abstract class ExpressionException extends java.lang.RuntimeException {
    @Deprecated
    public static <T> T opt1(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (InterpreterException e) {
            throw e;
        } catch (Exception e) {
            throw exception(expression -> new DalException(expression.left().getOperandPosition(), e));
        }
    }

    @Deprecated
    public static <T> T opt2(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw exception(expression -> new DalException(expression.right().getOperandPosition(), e));
        }
    }

    public java.lang.RuntimeException rethrow(DALExpression expression) {
        return thrower(expression);
    }

    abstract protected java.lang.RuntimeException thrower(DALExpression expression);

    public static ExpressionException exception(Function<DALExpression, java.lang.RuntimeException> thrower) {
        return new ExpressionException() {
            @Override
            protected java.lang.RuntimeException thrower(DALExpression expression) {
                return thrower.apply(expression);
            }
        };
    }

    public static ExpressionException illegalOperation(String message) {
        return exception(expression -> new DalException(message, expression.operator().getPosition()));
    }

    public static ExpressionException illegalOp2(String message) {
        return exception(expression -> new DalException(message, expression.right().getOperandPosition()));
    }

    public static ExpressionException illegalOp1(String message) {
        return exception(expression -> new DalException(message, expression.left().getOperandPosition()));
    }
}
