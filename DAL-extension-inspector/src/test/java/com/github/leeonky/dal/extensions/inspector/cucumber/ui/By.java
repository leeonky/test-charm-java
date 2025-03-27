package com.github.leeonky.dal.extensions.inspector.cucumber.ui;

public class By {
    protected String type;
    protected String value;

    public By(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public static By css(String css) {
        return new By(CSS, css);
    }

    public static By xpath(String xpath) {
        return new By(XPATH, xpath);
    }

    public String type() {
        return type;
    }

    public String value() {
        return value;
    }

    public static final String XPATH = "xpath", CSS = "css", TAG = "tag";

    @Override
    public String toString() {
        return String.format("by %s:%s", type, value);
    }
}