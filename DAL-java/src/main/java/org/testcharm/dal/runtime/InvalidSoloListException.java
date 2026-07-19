package org.testcharm.dal.runtime;

public class InvalidSoloListException extends RuntimeException {
    private final DALCollection<?> list;

    public InvalidSoloListException(String message, DALCollection<?> list) {
        super(message);
        this.list = list;
    }

    public DALCollection<?> list() {
        return list;
    }
}
