package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Element;
import com.github.leeonky.pf.AbstractRegion;
import com.github.leeonky.pf.Elements;

public class WatchesItem extends AbstractRegion<Element> {
    public WatchesItem(Element element) {
        super(element);
    }

    @Override
    public String toString() {
        return perform("css[.watches-item-content].text");
    }

    public Elements<Element> image() {
        return locate("css[img]");
    }

    public Elements<Element> download() {
        return locate("css[a]");
    }
}
