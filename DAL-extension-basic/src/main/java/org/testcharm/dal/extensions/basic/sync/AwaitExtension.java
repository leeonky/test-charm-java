package org.testcharm.dal.extensions.basic.sync;

import org.testcharm.dal.DAL;
import org.testcharm.dal.ast.opt.DALOperator;
import org.testcharm.dal.runtime.*;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;

public class AwaitExtension implements Extension {
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerMetaProperty("await", metaData -> new Await(metaData.data()))
                .registerDataRemark(Await.class, remarkData -> remarkData.data().value().within(remarkData.remark()))
                .registerOperator(Operators.MATCH, new AbstractOperation<Await, ExpectationFactory>() {
                    @Override
                    public Object operateObject(Data<Await> v1, DALOperator operator, Data<ExpectationFactory> v2,
                                                DALRuntimeContext context) {
                        return v1.value().await(data -> v2.value().create(operator, data).matches().value());
                    }
                })
                .registerOperator(Operators.EQUAL, new AbstractOperation<Await, ExpectationFactory>() {
                    @Override
                    public Object operateObject(Data<Await> v1, DALOperator operator, Data<ExpectationFactory> v2,
                                                DALRuntimeContext context) {
                        return v1.value().await(data -> v2.value().create(operator, data).equalTo().value());
                    }
                })
                .registerMetaProperty(Await.class, "every", metaData ->
                        (DataRemarkParameterAcceptor<Await>) s -> metaData.data().value().interval(s))
        ;
    }
}
