package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.InspectorElement;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Page;

public class WorkbenchPage extends Page<InspectorElement> {
    private final InspectorElement header;

    public WorkbenchPage(InspectorElement element, InspectorElement header) {
        super(element);
        this.header = header;
    }

    public InspectorElement getHeader() {
        return header;
    }

    public InspectorElement DAL() {
        return element.byPlaceholder("DAL expression");
    }
}
