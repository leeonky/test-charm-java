package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.ExpectationFactory;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.Operation;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.util.NumberType;
import com.github.leeonky.util.function.TriFunction;

import static com.github.leeonky.dal.runtime.Data.ResolvedMethods.instanceOf;
import static com.github.leeonky.dal.runtime.Operators.*;

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
        dal.getRuntimeContextBuilder().registerOperator(MATCH, new Operation() {

            @Override
            public boolean match(Data v1, DALOperator operator, Data v2, DALRuntimeContext context) {
                return v2.probeIf(instanceOf(ExpectationFactory.class));
            }

            @Override
            public Data operateData(Data v1, DALOperator operator, Data v2, DALRuntimeContext context) {
                return v1.trigger(((ExpectationFactory) v2.instance()).create(operator, v1).matches());
            }
        });
    }

    private void assertEqual(DAL dal) {
        dal.getRuntimeContextBuilder().registerOperator(EQUAL, new Operation() {

            @Override
            public boolean match(Data v1, DALOperator operator, Data v2, DALRuntimeContext context) {
                return v2.probeIf(instanceOf(ExpectationFactory.class));
            }

            @Override
            public Data operateData(Data v1, DALOperator operator, Data v2, DALRuntimeContext context) {
                return v1.trigger(((ExpectationFactory) v2.instance()).create(operator, v1).equalTo());
            }
        });
    }

    private void stringPlus(DAL dal) {
        dal.getRuntimeContextBuilder().registerOperator(PLUS, new Operation() {

            @Override
            public boolean match(Data v1, DALOperator operator, Data v2, DALRuntimeContext context) {
                return v1.probeIf(instanceOf(String.class)) || v2.probeIf(instanceOf(String.class));
            }

            @Override
            public Object operate(Data v1, DALOperator operator, Data v2, DALRuntimeContext context) {
                return String.valueOf(v1.instance()) + v2.instance();
            }
        });
    }

    private void numberCalculator(DAL dal, com.github.leeonky.dal.runtime.Operators operator,
                                  TriFunction<NumberType, Number, Number, Number> action) {
        dal.getRuntimeContextBuilder().registerOperator(operator, new Operation() {

            @Override
            public boolean match(Data v1, DALOperator operator, Data v2, DALRuntimeContext context) {
                return v1.probeIf(instanceOf(Number.class)) && v2.probeIf(instanceOf(Number.class));
            }

            @Override
            public Object operate(Data v1, DALOperator operator, Data v2, DALRuntimeContext context) {
                return action.apply(context.getNumberType(), (Number) v1.instance(), (Number) v2.instance());
            }
        });
    }
}
