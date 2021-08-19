package com.github.leeonky.util;

import static java.lang.String.format;

public class CannotSetElementByIndexException extends IllegalArgumentException {
    public CannotSetElementByIndexException(Class<?> type) {
        super(format("Cannot set element by index for %s", type.getName()));
    }
}
