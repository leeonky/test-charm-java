package org.testcharm.dal.runtime;

import org.testcharm.dal.ast.opt.DALOperator;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.util.BeanClass;

public abstract class AbstractOperation<T1, T2> implements Operation<T1, T2> {
    private BeanClass<T1> type1;
    private BeanClass<T2> type2;

    @SuppressWarnings("unchecked")
    public AbstractOperation() {
        BeanClass<AbstractOperation> type = BeanClass.createFrom(this).getSuper(AbstractOperation.class);
        type1 = (BeanClass<T1>) type.getTypeArguments(0).get();
        type2 = (BeanClass<T2>) type.getTypeArguments(1).get();
    }

    @Override
    public boolean match(Data<?> v1, DALOperator operator, Data<?> v2, DALRuntimeContext context) {
        return (type1.is(Object.class) || v1.instanceOf(type1.getType()))
                && (type2.is(Object.class) || v2.instanceOf(type2.getType()));
    }

    @Override
    public Data<?> operate(Data<T1> v1, DALOperator operator, Data<T2> v2, DALRuntimeContext context) {
        return context.data(operateObject(v1, operator, v2, context));
    }

    public Object operateObject(Data<T1> v1, DALOperator operator, Data<T2> v2, DALRuntimeContext context) {
        return operate(v1, operator, v2, context).value();
    }
}
