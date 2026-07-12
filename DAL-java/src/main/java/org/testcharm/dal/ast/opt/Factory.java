package org.testcharm.dal.ast.opt;

import org.testcharm.dal.ast.node.DALExpression;
import org.testcharm.dal.ast.node.ExecutableNode;
import org.testcharm.dal.ast.node.SchemaComposeNode;
import org.testcharm.dal.compiler.GeneratedForJacocoIgnore;
import org.testcharm.dal.compiler.Notations;
import org.testcharm.dal.runtime.*;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.interpreter.Notation;
import org.testcharm.util.function.TriFunction;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.testcharm.dal.runtime.DALException.locateError;

public class Factory {

    @GeneratedForJacocoIgnore
    private Factory() {
    }

    public static DALOperator logical(Notation<?, ?, ?, ?, ?> notation, ExpressionContextData.SupplierSupplierData logical) {
        return new Operator(Precedence.LOGICAL, notation, ExpressionContextData.adapt(logical), true, Operators.NA);
    }

    public static DALOperator plusSub(Notation<?, ?, ?, ?, ?> notation, ExpressionContextData.DataOptDataContextData plusSub, Operators type) {
        return new Operator(Precedence.PLUS_SUB, notation, ExpressionContextData.adapt(plusSub), false, type);
    }

    public static DALOperator mulDiv(Notation<?, ?, ?, ?, ?> notation, ExpressionContextData.DataOptDataContextData mulDiv, Operators type) {
        return new Operator(Precedence.MUL_DIV, notation, ExpressionContextData.adapt(mulDiv), false, type);
    }

    public static DALOperator comparator(Notation<?, ?, ?, ?, ?> notation, ExpressionContextData operation) {
        return new Operator(Precedence.COMPARISON, notation, operation, true, Operators.NA);
    }

    public static DALOperator unary(Notation<?, ?, ?, ?, ?> notation, ExpressionContextData unary) {
        return new Operator(Precedence.UNARY_OPERATION, notation, unary, true, Operators.NA) {
            @Override
            public String inspect(String node1, String node2) {
                return notation.getLabel() + node2;
            }
        };
    }

    public static DALOperator executable(Notation<?, ?, ?, ?, ?> notation) {
        return new DALOperator(Precedence.PROPERTY, notation.getLabel(), false, Operators.NA) {
            @Override
            public Data<?> calculate(DALExpression expression, DALRuntimeContext context) {
                return ((ExecutableNode) expression.right()).getValue(expression.left(), context);
            }

            @Override
            public String inspect(String node1, String node2) {
                return String.format("%s%s%s", node1, label, node2);
            }
        };
    }

    public static DALOperator is() {
        return new DALOperator(Precedence.VERIFICATION, Notations.Operators.IS.getLabel(), true, Operators.NA) {
            @Override
            public Data<?> calculate(DALExpression expression, DALRuntimeContext context) {
                return ((SchemaComposeNode) expression.right()).verify(expression.left(), context);
            }
        };
    }

    public static DALOperator which() {
        return new DALOperator(Precedence.WHICH, Notations.Operators.WHICH.getLabel(), true, Operators.NA) {
            @Override
            public Data<?> calculate(DALExpression expression, DALRuntimeContext context) {
                return expression.left().evaluateData(context).execute(() -> expression.right().evaluateData(context));
            }
        };
    }

    public static DALOperator dataRemark() {
        return new DALOperator(Precedence.REMARK_EXCLAMATION, "DATA_REMARK", false, Operators.NA) {

            @Override
            public Data<?> calculate(DALExpression expression, DALRuntimeContext context) {
                return context.invokeDataRemark(new RemarkData(expression.left().evaluateData(context), context,
                        (String) expression.right().evaluate(context)));
            }

            @Override
            public String inspect(String node1, String node2) {
                return node1 + "(" + node2 + ")";
            }
        };
    }

    public static DALOperator constRemark() {
        return new DALOperator(Precedence.REMARK_EXCLAMATION, "CONST_REMARK", false, Operators.NA) {

            @Override
            public Data<?> calculate(DALExpression expression, DALRuntimeContext context) {
                Data<?> leftValue = expression.left().evaluateData(context);
                Data<?> rightValue = expression.right().evaluateData(context);
                if (Objects.equals(leftValue.value(), rightValue.value()))
                    return leftValue;
                throw locateError(new DALRuntimeException(String.format("Incorrect const remark, const value was %s\nbut remark %s was %s",
                        leftValue.dump(), expression.right().inspect(), rightValue.dump())), expression.right().getPositionBegin());
            }

            @Override
            public String inspect(String node1, String node2) {
                return node1 + " " + node2;
            }
        };
    }

    public static DALOperator exclamation() {
        return new DALOperator(Precedence.REMARK_EXCLAMATION, "EXCLAMATION", false, Operators.NA) {

            @Override
            public Data<?> calculate(DALExpression expression, DALRuntimeContext context) {
                return context.invokeExclamations(new ExclamationData<>(expression.left().evaluateData(context),
                        expression.right(), context));
            }

            @Override
            public String inspect(String node1, String node2) {
                return node1 + node2;
            }
        };
    }

    public static DALOperator equal() {
        return new VerificationOperator(Notations.Operators.EQUAL.getLabel(), Operators.EQUAL);
    }

    public static DALOperator match() {
        return new VerificationOperator(Notations.Operators.MATCHER.getLabel(), Operators.MATCH);
    }

    public interface ExpressionContextData extends BiFunction<DALExpression, DALRuntimeContext, Data<?>> {
        static ExpressionContextData adapt(SupplierSupplierData operation) {
            return (expression, context) -> operation.apply(() -> expression.left().evaluateData(context),
                    () -> expression.right().evaluateData(context), expression.left().isAssertion());
        }

        static ExpressionContextData adapt(DataDataObject operation) {
            return (expression, context) -> context.data(operation.apply(expression.left().evaluateData(context), expression.right().evaluateData(context)));
        }

        static ExpressionContextData adapt(DataOptDataContextData operation) {
            return (expression, context) -> operation.apply(expression.left().evaluateData(context),
                    expression.operator(), expression.right().evaluateData(context), context);
        }

        static ExpressionContextData adapt(DataOptDataContextObj operation) {
            return (expression, context) -> {
                Data<?> left = expression.left().evaluateData(context);
                Data<?> right = expression.right().evaluateData(context);
                return context.data(operation.apply(left, expression.operator(), right, context));
            };
        }

        static ExpressionContextData adapt(DataContextData operation) {
            return (expression, context) -> operation.apply(expression.right().evaluateData(context), context);
        }

        static ExpressionContextData adapt(DataObject operation) {
            return (expression, context) -> context.data(operation.apply(expression.right().evaluate(context)));
        }

        interface SupplierSupplierData extends TriFunction<Supplier<Data<?>>, Supplier<Data<?>>, Boolean, Data<?>> {
        }

        interface DataOptDataContextData extends QuadFunction<Data<?>, DALOperator, Data<?>, DALRuntimeContext, Data<?>> {
        }

        interface DataOptDataContextObj extends QuadFunction<Data<?>, DALOperator, Data<?>, DALRuntimeContext, Object> {
        }

        interface DataContextData extends BiFunction<Data<?>, DALRuntimeContext, Data<?>> {
        }

        interface DataObject extends Function<Object, Object> {
        }

        interface DataDataObject extends BiFunction<Data<?>, Data<?>, Object> {
        }
    }

    static class Operator extends DALOperator {
        private final ExpressionContextData operation;

        public Operator(int precedence, Notation<?, ?, ?, ?, ?> notation,
                        ExpressionContextData operation, boolean needInspect, Operators type) {
            super(precedence, notation.getLabel(), needInspect, type);
            this.operation = operation;
        }

        @Override
        public Data<?> calculate(DALExpression expression, DALRuntimeContext context) {
            return operation.apply(expression, context);
        }
    }

    public interface QuadFunction<T1, T2, T3, T4, R> {
        R apply(T1 obj1, T2 obj2, T3 obj3, T4 obj4);
    }

    public static class VerificationOperator extends DALOperator {
        public VerificationOperator(String label, Operators type) {
            super(Precedence.VERIFICATION, label, true, type);
        }

        @Override
        public Data<?> calculate(DALExpression expression, DALRuntimeContext context) {
            return expression.right().verify(expression.operator(), expression.left(), context);
        }

        @Override
        public String inspect(String node1, String node2) {
            return String.format("%s%s %s", node1, label, node2);
        }
    }
}
