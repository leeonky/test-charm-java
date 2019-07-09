package com.github.leeonky.util;

class StringUtil {
    static String unCapitalize(String str) {
        return str.isEmpty() ? str : str.toLowerCase().substring(0, 1) + str.substring(1);
    }
}
