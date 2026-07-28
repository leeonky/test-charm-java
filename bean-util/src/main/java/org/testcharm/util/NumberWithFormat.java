package org.testcharm.util;

public class NumberWithFormat {
    public final Number number;
    public final NumberFormat format;

    public NumberWithFormat(int sign, int radix, Number number) {
        this.number = number;
        format = new NumberFormat(sign, radix);
    }
}
