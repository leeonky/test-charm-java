package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.Element;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Page;

public class WatchesItem extends Page<Element> {
    public WatchesItem(Element element) {
        super(element);
    }

    @Override
    public String toString() {
        return element.byCss(".watches-item-content").text();
    }

    public Element image() {
        return element.byCss("img");
    }

    public Element download() {
        return element.byCss("a");
    }
}
