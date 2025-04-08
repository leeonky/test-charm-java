package com.github.leeonky.pf;

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

    public static By text(String xpath) {
        return new By(TEXT, xpath);
    }

    public static By placeholder(String placeholder) {
        return new By(PLACEHOLDER, placeholder);
    }

    public String type() {
        return type;
    }

    public String value() {
        return value;
    }

    public static final String XPATH = "xpath", CSS = "css", TEXT = "text", PLACEHOLDER = "placeholder";

    @Override
    public String toString() {
        return String.format("%s{%s}", type, value);
    }
}