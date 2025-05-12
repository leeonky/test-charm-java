package com.github.leeonky.dal.runtime;

public class AssertionFailure extends DALException {
    public AssertionFailure(String message, int position) {
        super(message, position);
    }
}
