package com.github.leeonky.dal.runtime;

public class ListMappingElementAccessException extends DALRuntimeException {
    public ListMappingElementAccessException(int index, Throwable exception) {
        super(mappingIndexMessage(index), exception);
    }

    private static String mappingIndexMessage(int index) {
        return String.format("Mapping element[%d]:", index);
    }
}
