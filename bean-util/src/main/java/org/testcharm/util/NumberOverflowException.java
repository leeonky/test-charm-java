package org.testcharm.util;

public class NumberOverflowException extends RuntimeException {
    public NumberOverflowException(String content) {
        super(String.format("Cannot parse [%s] with the given postfix type", content));
    }
}
