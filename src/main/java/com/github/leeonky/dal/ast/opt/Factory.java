package com.github.leeonky.dal.ast.opt;

import com.github.leeonky.dal.ast.node.DALExpression;
import com.github.leeonky.dal.ast.node.ExecutableNode;
import com.github.leeonky.dal.ast.node.SchemaComposeNode;
import com.github.leeonky.dal.compiler.Notations;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RemarkData;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.RuntimeData;
import com.github.leeonky.dal.runtime.RuntimeException;
import com.github.leeonky.interpreter.Notation;
import com.github.leeonky.util.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Factory {
    public static DALOperator logical(Notation<?, ?, ?, ?, ?> notation, ExpressionContextData.SupplierSupplierData logical) {
        return new Operator(Precedence.LOGICAL, notation, ExpressionContextData.adapt(logical), true);
    }

    public static DALOperator plusSub(Notation<?, ?, ?, ?, ?> notation, ExpressionContextData.DataDataContextData plusSub) {
        return new Operator(Precedence.PLUS_SUB, notation, ExpressionContextData.adapt(plusSub), false);
    }

    public static DALOperator mulDiv(Notation<?, ?, ?, ?, ?> notation, ExpressionContextData.DataDataContextData mulDiv) {
        return new Operator(Precedence.MUL_DIV, notation, ExpressionContextData.adapt(mulDiv), false);
    }

    public static DALOperator comparator(Notation<?, ?, ?, ?, ?> notation, ExpressionContextData operation) {
        return new Operator(Precedence.COMPARISON, notation, operation, true);
    }

    public static DALOperator unary(Notation<?, ?, ?, ?, ?> notation, ExpressionContextData unary) {
        return new Operator(Precedence.UNARY_OPERATION, notation, unary, true) {
            @Override
            public String inspect(String node1, String node2) {
                return notation.getLabel() + node2;
            }
        };
    }

    public static DALOperator executable(Notation<?, ?, ?, ?, ?> notation) {
        return new DALOperator(Precedence.PROPERTY, notation.getLabel(), false) {
            @Override
            public Data calculateData(DALExpression expression, DALRuntimeContext context) {
                return ((ExecutableNode) expression.right()).getValue(expression.left(), context);
            }

            @Override
            public String inspect(String node1, String node2) {
                return String.format("%s%s%s", node1, label, node2);
            }
        };
    }

    public static DALOperator is() {
        return new DALOperator(Precedence.COMPARISON, Notations.Operators.IS.getLabel(), true) {
            @Override
            public Data calculateData(DALExpression expression, DALRuntimeContext context) {
                return ((SchemaComposeNode) expression.right()).verify(expression.left(), context);
            }
        };
    }

    public static DALOperator which() {
        return new DALOperator(Precedence.WHICH, Notations.Operators.WHICH.getLabel(), true) {
            @Override
            public Object calculate(DALExpression expression, DALRuntimeContext context) {
                try {
                    return expression.left().evaluateData(context).execute(() -> expression.right().evaluate(context));
                } catch (IllegalStateException e) {
                    throw new RuntimeException(e.getMessage(), getPosition());
                }
            }
        };
    }

    public static DALOperator dataRemark() {
        return new DALOperator(Precedence.REMARK_EXCLAMATION, "DATA_REMARK", false) {

            @Override
            public Data calculateData(DALExpression expression, DALRuntimeContext context) {
                return context.invokeDataRemark(new RemarkData(expression.left().evaluateData(context),
                        expression.left(), expression.right(), context, expression.operator()));
            }

            @Override
            public String inspect(String node1, String node2) {
                return node1 + "(" + node2 + ")";
            }
        };
    }


    public static DALOperator exclamation() {
        return new DALOperator(Precedence.REMARK_EXCLAMATION, "EXCLAMATION", false) {

            @Override
            public Data calculateData(DALExpression expression, DALRuntimeContext context) {
                return context.invokeExclamations(new RuntimeData(expression.left().evaluateData(context),
                        expression.left(), expression.right(), context, expression.operator()));
            }

            @Override
            public String inspect(String node1, String node2) {
                return node1 + node2;
            }
        };
    }

    public interface ExpressionContextData extends BiFunction<DALExpression, DALRuntimeContext, Data> {
        static ExpressionContextData adapt(SupplierSupplierData operation) {
            return (expression, context) -> context.wrap(operation.apply(() -> expression.left().evaluateData(context),
                    () -> expression.right().evaluateData(context)).instance());
        }

        static ExpressionContextData adapt(DataDataObject operation) {
            return (expression, context) -> context.wrap(operation.apply(expression.left().evaluateData(context), expression.right().evaluateData(context)));
        }

        static ExpressionContextData adapt(DataDataContextData operation) {
            return (expression, context) -> operation.apply(expression.left().evaluateData(context), expression.right().evaluateData(context), context);
        }

        static ExpressionContextData adapt(DataContextData operation) {
            return (expression, context) -> operation.apply(expression.right().evaluateData(context), context);
        }

        static ExpressionContextData adapt(DataObject operation) {
            return (expression, context) -> context.wrap(operation.apply(expression.right().evaluate(context)));
        }

        interface SupplierSupplierData extends BiFunction<Supplier<Data>, Supplier<Data>, Data> {
        }

        interface DataDataContextData extends TriFunction<Data, Data, DALRuntimeContext, Data> {
        }

        interface DataContextData extends BiFunction<Data, DALRuntimeContext, Data> {
        }

        interface DataObject extends Function<Object, Object> {
        }

        interface DataDataObject extends BiFunction<Data, Data, Object> {
        }
    }

    static class Operator extends DALOperator {
        private final ExpressionContextData operation;

        public Operator(int precedence, Notation<?, ?, ?, ?, ?> notation,
                        ExpressionContextData operation, boolean needInspect) {
            super(precedence, notation.getLabel(), needInspect);
            this.operation = operation;
        }

        @Override
        public Data calculateData(DALExpression expression, DALRuntimeContext context) {
            return operation.apply(expression, context);
        }
    }
}
