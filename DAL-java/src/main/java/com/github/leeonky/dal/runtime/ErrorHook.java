package com.github.leeonky.dal.runtime;

public interface ErrorHook {
    boolean handle(Object input, String code, Throwable error);
}
