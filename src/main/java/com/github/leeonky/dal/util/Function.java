package com.github.leeonky.dal.util;

import java.util.function.Predicate;

public class Function {
    public static <T> Predicate<T> not(Predicate<T> t) {
        return t.negate();
    }
}
