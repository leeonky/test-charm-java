package com.github.leeonky.dal.extensions.basic.list;

import com.github.leeonky.dal.runtime.AssertionFailure;

public class NotReadyException extends AssertionFailure {
    public NotReadyException(String message, int positionBegin) {
        super(message, positionBegin);
    }
}
