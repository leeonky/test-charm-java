package com.github.leeonky.dal.extensions.inspector.cucumber.page;

public class By {
    protected String type;
    protected Object value;

    public By(String type, Object value) {
        this.type = type;
        this.value = value;
    }

    public String type() {
        return type;
    }

    public Object value() {
        return value;
    }

    public static final String XPATH = "xpath", CSS = "css", TAG = "tag";

    @Override
    public String toString() {
        return String.format("by %s:%s", type, value);
    }
}