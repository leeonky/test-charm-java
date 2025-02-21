package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import java.util.Objects;

public class OutputPage {
    private final Panel panel;
    private final String type;

    public OutputPage(Panel panel, String type) {
        this.panel = panel;
        this.type = type;
    }

    public boolean isType(String type) {
        return Objects.equals(this.type, type);
    }

    @Override
    public String toString() {
        return panel.text();
    }
}
