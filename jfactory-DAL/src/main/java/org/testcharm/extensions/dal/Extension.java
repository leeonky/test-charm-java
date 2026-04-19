package org.testcharm.extensions.dal;

import org.testcharm.dal.DAL;
import org.testcharm.dal.runtime.RemarkData;
import org.testcharm.dal.runtime.RuntimeDataHandler;
import org.testcharm.jfactory.JFactoryDAL;

public class Extension implements org.testcharm.dal.runtime.Extension {
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerDataRemark(JFactoryDAL.JFactoryCollectorWithoutTraits.class,
                (RuntimeDataHandler<RemarkData<JFactoryDAL.JFactoryCollectorWithoutTraits>>)
                        remarkData -> remarkData.data().map(collector -> collector.addTrait(remarkData.remark())));
    }
}
