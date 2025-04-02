package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.Element;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Page;

import static com.github.leeonky.dal.extensions.inspector.cucumber.ui.By.css;

public class WatchesItem extends Page<Element> {
    public WatchesItem(Element element) {
        super(element);
    }

    @Override
    public String toString() {
        return region.findBy(css(".watches-item-content")).text();
    }

    public Element image() {
        return region.findBy(css("img"));
    }

    public Element download() {
        return region.findBy(css("a"));
    }
}
