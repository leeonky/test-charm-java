package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.Element;
import com.github.leeonky.dal.extensions.inspector.cucumber.Tab;
import com.github.leeonky.dal.extensions.inspector.cucumber.Tabs;

public class WorkbenchPage extends Tab {

    private final Tabs<WorkspacePage, Element> workspaces;

    public WorkbenchPage(Element header, Element element) {
        super(header, element);
        workspaces = new Tabs<WorkspacePage, Element>(element.byCss(".workspaces")) {
        };
    }

    public Element DAL() {
        return element.byPlaceholder("DAL expression");
    }

    public OutputPage Current() {
        return workspaces.getCurrent().Current();
    }

    public OutputPage Output(String name) {
        return workspaces.getCurrent().Output(name);
    }

    public void execute() {
        workspaces.getCurrent().execute();
    }
}
