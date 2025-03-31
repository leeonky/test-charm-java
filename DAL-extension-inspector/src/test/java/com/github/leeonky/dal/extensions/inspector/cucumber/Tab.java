package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Page;

public class Tab extends Page<Element> {
    private final Element header;

    public Tab(Element header, Element element) {
        super(element);
        this.header = header;
    }

    public Element getHeader() {
        return header;
    }
}
