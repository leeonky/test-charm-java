package com.github.leeonky.dal.extensions.inspector.cucumber.page;

public class OutputPage {
    private final Browser browser;
    private final String type;

    public OutputPage(Browser browser, String type) {
        this.browser = browser;
        this.type = type;
    }

    public boolean isType(String type) {
        return false;
    }

    @Override
    public String toString() {
        return browser.byPlaceholder(type).text();
    }
}
