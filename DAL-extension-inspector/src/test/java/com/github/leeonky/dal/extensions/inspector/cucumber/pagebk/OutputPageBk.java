package com.github.leeonky.dal.extensions.inspector.cucumber.pagebk;

import lombok.Getter;

import java.util.Objects;

public class OutputPageBk {
    private final Panel panel;

    @Getter
    private final Panel header;

    public OutputPageBk(Panel panel, Panel header) {
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

    public Panel getContent() {
        return panel;
    }
}
