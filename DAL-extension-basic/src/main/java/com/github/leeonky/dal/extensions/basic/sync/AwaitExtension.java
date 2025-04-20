package com.github.leeonky.dal.extensions.basic.sync;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;

public class AwaitExtension implements Extension {
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerMetaProperty("await", metaData -> new Await(metaData.data()))
                .registerDataRemark(Await.class, remarkData -> remarkData.data().value().within(remarkData.remark()))
                .registerOperator(Operators.MATCH, new AwaitVerification() {
                    @Override
                    public Object operate(Data<Await> v1, DALOperator operator, Data<ExpectationFactory> v2,
                                          DALRuntimeContext context) {
                        return v1.value().await(data -> v2.value().create(operator, data).matches().value());
                    }
                })
                .registerOperator(Operators.EQUAL, new AwaitVerification() {
                    @Override
                    public Object operate(Data<Await> v1, DALOperator operator, Data<ExpectationFactory> v2,
                                          DALRuntimeContext context) {
                        return v1.value().await(data -> v2.value().create(operator, data).equalTo().value());
                    }
                })
                .registerMetaProperty(Await.class, "every", metaData ->
                        (DataRemarkParameterAcceptor<Await>) s -> metaData.data().value().interval(s))
        ;
    }

    private static abstract class AwaitVerification implements Operation<Await, ExpectationFactory> {
        @Override
        public boolean match(Data<?> v1, DALOperator operator, Data<?> v2, DALRuntimeContext context) {
            return v1.instanceOf(Await.class) && v2.instanceOf(ExpectationFactory.class);
        }
    }
}
