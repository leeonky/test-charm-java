package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import lombok.Getter;

import java.util.Objects;

public class OutputPage {
    private final Panel panel;

    @Getter
    private final Panel header;

    public OutputPage(Panel panel, Panel header) {
        this.panel = panel;
        this.header = header;
    }

    public boolean isType(String type) {
        return Objects.equals(header.text(), type);
    }

    @Override
    public String toString() {
        return panel.text();
    }
}
