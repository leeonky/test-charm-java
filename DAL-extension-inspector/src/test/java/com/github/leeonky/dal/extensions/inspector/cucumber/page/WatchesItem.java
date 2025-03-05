package com.github.leeonky.dal.extensions.inspector.cucumber.page;

public class WatchesItem {
    private final Panel panel;

    public WatchesItem(Panel panel) {
        this.panel = panel;
    }

    @Override
    public String toString() {
        return panel.byCss(".watches-item-content").text();
    }
}
