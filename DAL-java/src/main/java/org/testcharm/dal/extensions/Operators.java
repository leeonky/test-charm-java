package org.testcharm.dal.extensions;

import org.testcharm.dal.DAL;
import org.testcharm.dal.ast.opt.DALOperator;
import org.testcharm.dal.runtime.AbstractOperation;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.ExpectationFactory;
import org.testcharm.dal.runtime.Extension;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.util.NumberType;
import org.testcharm.util.function.TriFunction;

import static org.testcharm.dal.runtime.Operators.*;

public class Operators implements Extension {

    @Override
    public int order() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void extend(DAL dal) {
        numberCalculator(dal, PLUS, NumberType::plus);
        stringPlus(dal);
        numberCalculator(dal, SUB, NumberType::subtract);
        numberCalculator(dal, MUL, NumberType::multiply);
        numberCalculator(dal, DIV, NumberType::divide);

        assertEqual(dal);
        assertMatch(dal);
    }

    private void assertMatch(DAL dal) {
        dal.getRuntimeContextBuilder().registerOperator(MATCH, new AbstractOperation<Object, ExpectationFactory>() {

            @Override
            public Data<?> operate(Data<Object> v1, DALOperator operator, Data<ExpectationFactory> v2, DALRuntimeContext context) {
                v2.value().create(operator, v1).matches().value();
                return v1;
            }
        });
    }

    private void assertEqual(DAL dal) {
        dal.getRuntimeContextBuilder().registerOperator(EQUAL, new AbstractOperation<Object, ExpectationFactory>() {

            @Override
            public Data<?> operate(Data<Object> v1, DALOperator operator, Data<ExpectationFactory> v2, DALRuntimeContext context) {
                v2.value().create(operator, v1).equalTo().value();
                return v1;
            }
        });
    }

    private void stringPlus(DAL dal) {
        dal.getRuntimeContextBuilder().registerOperator(PLUS, new AbstractOperation<Object, Object>() {

            @Override
            public boolean match(Data<?> v1, DALOperator operator, Data<?> v2, DALRuntimeContext context) {
                return v1.instanceOf(String.class) || v2.instanceOf(String.class);
            }

            @Override
            public Object operateObject(Data<Object> v1, DALOperator operator, Data<Object> v2, DALRuntimeContext context) {
                return String.valueOf(v1.value()) + v2.value();
            }
        });
    }

    private void numberCalculator(DAL dal, org.testcharm.dal.runtime.Operators operator,
                                  TriFunction<NumberType, Number, Number, Number> action) {
        dal.getRuntimeContextBuilder().registerOperator(operator, new AbstractOperation<Number, Number>() {

            @Override
            public Object operateObject(Data<Number> v1, DALOperator operator, Data<Number> v2, DALRuntimeContext context) {
                return action.apply(context.getNumberType(), v1.value(), v2.value());
            }
        });
    }
}
