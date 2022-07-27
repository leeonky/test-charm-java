package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.type.ExtensionName;
import com.github.leeonky.util.NumberParser;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(StaticMethods.class);
    }

    public static class StaticMethods {
        public static final NumberParser NUMBER_PARSER = new NumberParser();

        public static Number number(String content) {
            return NUMBER_PARSER.parse(content);
        }

        @ExtensionName("byte")
        public static byte toByte(String content) {
            return Byte.parseByte(content);
        }

        @ExtensionName("short")
        public static short toShort(String content) {
            return Short.parseShort(content);
        }

        @ExtensionName("int")
        public static int toInt(String content) {
            return Integer.parseInt(content);
        }

        @ExtensionName("long")
        public static long toLong(String content) {
            return Long.parseLong(content);
        }

        @ExtensionName("float")
        public static float toFloat(String content) {
            return Float.parseFloat(content);
        }

        @ExtensionName("double")
        public static double toDouble(String content) {
            return Double.parseDouble(content);
        }

        public static BigInteger bigInt(String content) {
            return new BigInteger(content);
        }

        public static BigDecimal decimal(String content) {
            return new BigDecimal(content);
        }
    }
}
