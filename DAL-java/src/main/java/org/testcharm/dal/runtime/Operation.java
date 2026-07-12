package org.testcharm.dal.runtime;

import org.testcharm.dal.ast.opt.DALOperator;

public interface Operation<T1, T2> {
    boolean match(Data<?> v1, DALOperator operator, Data<?> v2, RuntimeContextBuilder.DALRuntimeContext context);

    Data<?> operate(Data<T1> v1, DALOperator operator, Data<T2> v2, RuntimeContextBuilder.DALRuntimeContext context);
}
