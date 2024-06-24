package com.github.leeonky.dal.extensions.basic.list;

import com.github.leeonky.dal.runtime.DalException;

public class NotReadyException extends DalException {
    public NotReadyException(String message, int positionBegin) {
        super(message, positionBegin);
    }
}
