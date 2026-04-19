package org.testcharm.extensions.dal;

import org.testcharm.dal.DAL;
import org.testcharm.dal.ast.opt.DALOperator;
import org.testcharm.dal.runtime.*;
import org.testcharm.jfactory.JFactoryCollector;
import org.testcharm.jfactory.JFactoryDAL;

public class JFactoryDalExtension implements org.testcharm.dal.runtime.Extension {
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerOperator(Operators.EQUAL, new Operation<JFactoryCollector, ExpectationFactory>() {
            @Override
            public boolean match(Data<?> v1, DALOperator operator, Data<?> v2, RuntimeContextBuilder.DALRuntimeContext context) {
                return v1.instanceOf(JFactoryCollector.class) && v2.instanceOf(ExpectationFactory.class);
            }

            @Override
            public Data<?> operateData(Data<JFactoryCollector> v1, DALOperator operator, Data<ExpectationFactory> v2,
                                       RuntimeContextBuilder.DALRuntimeContext context) {
                v1.value().raw();
                return v2.value().create(operator, v1).equalTo();
            }
        });

        dal.getRuntimeContextBuilder()
                .registerDataRemark(JFactoryCollector.class, remarkData ->
                        remarkData.data().value().traitsSpec(remarkData.remark().split(", |,| ")))
                .registerExclamation(JFactoryCollector.class, runtimeData -> {
                    runtimeData.data().value().intently();
                    return runtimeData.data().value();
                })
                .registerDataRemark(JFactoryDAL.JFactoryCollectorWithoutTraits.class,
                        (RuntimeDataHandler<RemarkData<JFactoryDAL.JFactoryCollectorWithoutTraits>>)
                                remarkData -> remarkData.data().map(collector -> collector.addTrait(remarkData.remark())))
        ;
    }
}
