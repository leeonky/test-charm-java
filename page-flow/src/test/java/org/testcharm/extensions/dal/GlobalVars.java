package org.testcharm.extensions.dal;

import org.testcharm.dal.DAL;
import org.testcharm.dal.runtime.Extension;

import java.util.HashMap;
import java.util.Map;

public class GlobalVars implements Extension {
    private Map<String, Object> globals = new HashMap<>();

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerMetaProperty("global", metaData -> globals);
    }
}
