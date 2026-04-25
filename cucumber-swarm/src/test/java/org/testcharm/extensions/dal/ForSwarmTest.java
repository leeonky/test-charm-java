package org.testcharm.extensions.dal;

import org.testcharm.dal.DAL;
import org.testcharm.dal.runtime.Extension;

public class ForSwarmTest implements Extension {
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(ForSwarmTest.class);
    }

    public static String normalize(String input) {
        return input.replaceAll("(?m)^\\s*\\d+m \\d+\\.\\d+s\\s*$", "").replaceAll("\\u001B\\[[;\\d]*m", "").trim();
    }
}
