package com.github.leeonky.dal.runtime;

public class UserRuntimeException extends RuntimeException {
    public UserRuntimeException(Throwable e) {
        super(e);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
