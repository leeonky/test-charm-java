package com.github.leeonky.dal.extensions.basic;

import java.math.BigDecimal;

public class TimeUtil {
    public static int parseTime(String s) {
        if (s.endsWith("ms"))
            return Integer.parseInt(s.replace("ms", ""));
        else if (s.endsWith("s"))
            return new BigDecimal(s.replace("s", "")).multiply(new BigDecimal(1000)).intValue();
        else
            throw new IllegalArgumentException("unknown time format: " + s);
    }
}
