package com.github.leeonky.dal.runtime;

import java.lang.RuntimeException;

public class UserRuntimeException extends RuntimeException {
    public UserRuntimeException(Exception e) {
        super(e);
    }
}
