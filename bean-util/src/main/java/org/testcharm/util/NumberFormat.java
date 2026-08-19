package org.testcharm.util;

public class NumberFormat {
    public final int sign;
    public final int radix;

    public NumberFormat(int sign, int radix) {
        this.sign = sign < 0 ? -1 : 1;
        this.radix = radix;
    }

    public String format(Number number) {
        String radixChar;
        switch (radix) {
            case 2:
                radixChar = "0b";
                break;
            case 16:
                radixChar = "0x";
                break;
            case 8:
                radixChar = "0";
                break;
            default:
                radixChar = "";
        }
        if (number instanceof Byte) {
            if (sign > 0)
                return radixChar + Integer.toString(Byte.toUnsignedInt((Byte) number), radix).toUpperCase();
            return '-' + radixChar + Integer.toString(sign * number.intValue(), radix).toUpperCase();
        }
        if (number instanceof Short) {
            if (sign > 0)
                return radixChar + Integer.toString(Short.toUnsignedInt((Short) number), radix).toUpperCase();
            return '-' + radixChar + Integer.toString(sign * number.intValue(), radix).toUpperCase();
        }

        if (number instanceof Integer) {
            if (sign > 0)
                return radixChar + Long.toString(Integer.toUnsignedLong((Integer) number), radix).toUpperCase();
            return '-' + radixChar + Long.toString(sign * number.longValue(), radix).toUpperCase();
        }
        return number.toString();
    }
}