package org.testcharm.extensions.dal;

import org.testcharm.cucumber.swarm.master.MasterDataMapper;
import org.testcharm.dal.DAL;
import org.testcharm.dal.runtime.Extension;
import org.testcharm.dal.runtime.MetaData;
import org.testcharm.dal.runtime.RuntimeHandler;

public class MasterDB implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerMetaProperty("DB", new RuntimeHandler<MetaData<?>>() {
            @Override
            public Object handle(MetaData<?> metaData) {
                return MasterDataMapper.getInstanceForTest();
            }
        });
    }
}
