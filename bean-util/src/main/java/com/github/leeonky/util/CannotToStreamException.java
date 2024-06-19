package com.github.leeonky.util;

public class CannotToStreamException extends IllegalArgumentException {
    public CannotToStreamException(Object object) {
        super("`" + object + "` is not collection or array");
    }
}
