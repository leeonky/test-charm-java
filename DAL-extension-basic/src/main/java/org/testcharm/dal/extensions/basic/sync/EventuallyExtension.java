package org.testcharm.dal.extensions.basic.sync;

import org.testcharm.dal.DAL;
import org.testcharm.dal.ast.opt.DALOperator;
import org.testcharm.dal.runtime.*;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;

public class EventuallyExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerMetaProperty("eventually", metaData -> new Eventually(metaData.data()))
                .registerOperator(Operators.MATCH, new EventuallyVerification())
                .registerOperator(Operators.EQUAL, new EventuallyVerification())
                .registerMetaProperty(Eventually.class, "in", metaData ->
                        (DataRemarkParameterAcceptor<Eventually>) s -> metaData.data().value().within(s))
                .registerMetaProperty(Eventually.class, "every", metaData ->
                        (DataRemarkParameterAcceptor<Eventually>) s -> metaData.data().value().interval(s))
        ;
    }

    private static class EventuallyVerification extends AbstractOperation<Eventually, Object> {

        @Override
        public Object operateObject(Data<Eventually> v1, DALOperator operator, Data<Object> v2, DALRuntimeContext context) {
            return v1.value().verify(operator, v2, context);
        }
    }
}
