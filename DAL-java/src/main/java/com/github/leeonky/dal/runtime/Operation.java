package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;

public interface Operation<T1, T2> {
    /**
     * @param v1       Type Data support short-circuit evaluation with operators like && or ||
     * @param operator
     * @param v2
     * @param context
     * @return
     */
    boolean match(Data<?> v1, DALOperator operator, Data<?> v2, DALRuntimeContext context);

    default Data<?> operateData(Data<T1> v1, DALOperator operator, Data<T2> v2, DALRuntimeContext context) {
        return context.data(operate(v1, operator, v2, context));
    }

    default Object operate(Data<T1> v1, DALOperator operator, Data<T2> v2, DALRuntimeContext context) {
        return operateData(v1, operator, v2, context).instance();
    }
}
