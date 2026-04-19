package org.testcharm.extensions.dal;

import org.testcharm.dal.DAL;
import org.testcharm.jfactory.JFactoryCollector;
import org.testcharm.util.Collector;

public class ExtensionForTest implements org.testcharm.dal.runtime.Extension {
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerMetaProperty(JFactoryCollector.class, "properties",
                        runtimeData -> runtimeData.data().value().properties())
                .registerMetaProperty(Collector.class, "build",
                        runtimeData -> runtimeData.data().value().build())
        ;
    }
}
