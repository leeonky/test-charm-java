package org.testcharm.dal.extensions;

import org.testcharm.dal.DAL;
import org.testcharm.dal.runtime.Extension;

import java.util.Objects;

public class ArrayMethods implements Extension {
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(ArrayMethods.class);
    }

    public static boolean contains(Object[] array, Object e) {
        for (Object o : array) {
            if (Objects.equals(o, e))
                return true;
        }
        return false;
    }
}
