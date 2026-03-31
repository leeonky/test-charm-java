package org.testcharm.extensions.dal;

import org.testcharm.cucumber.restful.RequestCollector;
import org.testcharm.dal.DAL;

public class Extension implements org.testcharm.dal.runtime.Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerMetaProperty(RequestCollector.class, "headers", metaData -> metaData.data().value().headerCollector())
                .registerMetaProperty(RequestCollector.class, "files", metaData -> metaData.data().value().files());
    }
}
