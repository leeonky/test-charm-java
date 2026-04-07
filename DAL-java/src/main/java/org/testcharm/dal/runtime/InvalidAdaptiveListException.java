package org.testcharm.dal.runtime;

public class InvalidAdaptiveListException extends RuntimeException {
    private final DALCollection<?> list;

    public InvalidAdaptiveListException(String message, DALCollection<?> list) {
        super(message);
        this.list = list;
    }

    public DALCollection<?> list() {
        return list;
    }
}
