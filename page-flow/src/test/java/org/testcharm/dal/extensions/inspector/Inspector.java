package org.testcharm.dal.extensions.inspector;

import org.testcharm.dal.DAL;
import org.testcharm.dal.runtime.Data;

import java.util.Map;

public class Inspector {

    public static void watch(DAL dal, String property, Data<?> value) {
        Map<String, Object> global = dal.evaluate(new Object(), "::global");
        global.put(property, value.value());
    }
}
