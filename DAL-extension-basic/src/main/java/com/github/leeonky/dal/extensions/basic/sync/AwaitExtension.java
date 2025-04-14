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
                .registerDataRemark(Await.class, remarkData -> remarkData.data().map(
                        data -> ((Await) data).within(remarkData.remark())))
                .registerOperator(Operators.MATCH, new AwaitVerification() {
                    @Override
                    public Object operate(Data<?> v1, DALOperator operator, Data<?> v2, DALRuntimeContext context) {
                        return ((Await) v1.instance()).await(data ->
                                ((ExpectationFactory) v2.instance()).create(operator, data).matches().instance());
                    }
                })
                .registerOperator(Operators.EQUAL, new AwaitVerification() {
                    @Override
                    public Object operate(Data<?> v1, DALOperator operator, Data<?> v2, DALRuntimeContext context) {
                        return ((Await) v1.instance()).await(data ->
                                ((ExpectationFactory) v2.instance()).create(operator, data).equalTo().instance());
                    }
                })
                .registerMetaProperty(Await.class, "every", metaData ->
                        (DataRemarkParameterAcceptor<Await>) s -> ((Await) metaData.data().instance()).interval(s))
        ;
    }

    private static abstract class AwaitVerification implements Operation {
        @Override
        public boolean match(Data<?> v1, DALOperator operator, Data<?> v2, DALRuntimeContext context) {
            return v1.instanceOf(Await.class) && v2.instanceOf(ExpectationFactory.class);
        }
    }
}
