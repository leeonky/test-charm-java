package com.github.leeonky.dal.extensions.inspector.cucumber.page;

public class OutputPage {
    private final Panel panel;
    private final String type;

    public OutputPage(Panel panel, String type) {
        this.panel = panel;
        this.type = type;
    }

    public boolean isType(String type) {
        return false;
    }

    @Override
    public String toString() {
        return panel.byPlaceholder(type).text();
    }
}
