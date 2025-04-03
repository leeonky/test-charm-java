package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Element;
import com.github.leeonky.dal.uiat.Region;

import static com.github.leeonky.dal.uiat.By.css;

public class WatchesItem extends Region<Element> {
    public WatchesItem(Element element) {
        super(element);
    }

    @Override
    public String toString() {
        return element.findBy(css(".watches-item-content")).text();
    }

    public Element image() {
        return element.findBy(css("img"));
    }

    public Element download() {
        return element.findBy(css("a"));
    }
}
