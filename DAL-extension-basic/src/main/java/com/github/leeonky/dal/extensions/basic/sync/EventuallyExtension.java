package com.github.leeonky.dal.extensions.basic.sync;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.Operation;
import com.github.leeonky.dal.runtime.Operators;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;

public class EventuallyExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerMetaProperty("eventually", metaData -> new Eventually(metaData.data()))
                .registerOperator(Operators.MATCH, new EventuallyVerification())
                .registerOperator(Operators.EQUAL, new EventuallyVerification())
        ;
    }

    private static class EventuallyVerification implements Operation {
        @Override
        public boolean match(Data v1, DALOperator operator, Data v2, DALRuntimeContext context) {
            return v1.instance() instanceof Eventually;
        }

        @Override
        public Data operate(Data v1, DALOperator operator, Data v2, DALRuntimeContext context) {
            return ((Eventually) v1.instance()).verify(operator, v2, context);
        }
    }
}
