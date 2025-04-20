package com.github.leeonky.dal.extensions.basic.sync;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;

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

    private static class EventuallyVerification implements Operation<Eventually, Object> {
        @Override
        public boolean match(Data<?> v1, DALOperator operator, Data<?> v2, DALRuntimeContext context) {
            return v1.instanceOf(Eventually.class);
        }

        @Override
        public Object operate(Data<Eventually> v1, DALOperator operator, Data<Object> v2, DALRuntimeContext context) {
            return v1.value().verify(operator, v2, context);
        }
    }
}
