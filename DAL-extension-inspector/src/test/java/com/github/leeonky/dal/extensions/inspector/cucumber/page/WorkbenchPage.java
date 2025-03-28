package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.Element;

public class WorkbenchPage extends Tab {

    public WorkbenchPage(Element header, Element element) {
        super(header, element);
    }

    public Element DAL() {
        return element.byPlaceholder("DAL expression");
    }
}
