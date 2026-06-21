package org.testcharm.dal.runtime;

import java.util.List;

public class AmbiguousFieldException extends RuntimeException {
    private final List<String> fields;
    private final Class<?> type;

    public AmbiguousFieldException(List<String> fields, Class<?> type) {
        this.fields = fields;
        this.type = type;
    }

    public List<String> getFields() {
        return fields;
    }

    public Class<?> getType() {
        return type;
    }
}
