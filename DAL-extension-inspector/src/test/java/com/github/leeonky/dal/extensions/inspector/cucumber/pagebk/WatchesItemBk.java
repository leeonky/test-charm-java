package com.github.leeonky.dal.extensions.inspector.cucumber.pagebk;

public class WatchesItemBk {
    private final Panel panel;

    public WatchesItemBk(Panel panel) {
        this.panel = panel;
    }

    @Override
    public String toString() {
        return panel.byCss(".watches-item-content").text();
    }

    public Panel image() {
        return panel.byCss("img");
    }

    public Panel download() {
        return panel.byCss("a");
    }
}
