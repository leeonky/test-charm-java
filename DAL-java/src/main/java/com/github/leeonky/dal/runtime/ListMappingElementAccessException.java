package com.github.leeonky.dal.runtime;

public class ListMappingElementAccessException extends java.lang.RuntimeException {
    private final int index;
    private Throwable exception;

    public ListMappingElementAccessException(int index, Throwable exception) {
        super();
        this.index = index;
        this.exception = exception;
    }

    //TODO refactor
    @Deprecated
    public DalException toDalError(int position) {
        return new DalException(mappingIndexMessage(), position, exception);
    }

    @Deprecated
    public String mappingIndexMessage() {
        return String.format("Mapping element[%d]:", index);
    }

    public Throwable exception() {
        return exception;
    }

}
