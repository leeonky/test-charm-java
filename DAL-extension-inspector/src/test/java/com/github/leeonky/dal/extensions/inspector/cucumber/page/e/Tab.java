package com.github.leeonky.dal.extensions.inspector.cucumber.page.e;

import com.github.leeonky.pf.Region;

public class Tab extends Region<Element> {
    private final Element header;

    public Tab(Element header, Element element) {
        super(element);
        this.header = header;
    }

    public Element getHeader() {
        return header;
    }
}
