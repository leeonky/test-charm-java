package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.Element;
import com.github.leeonky.dal.extensions.inspector.cucumber.Tab;
import com.github.leeonky.dal.extensions.inspector.cucumber.Tabs;

import static com.github.leeonky.dal.extensions.inspector.cucumber.ui.By.css;
import static com.github.leeonky.dal.extensions.inspector.cucumber.ui.By.placeholder;

public class WorkbenchPage extends Tab {

    private final Tabs<WorkspacePage, Element> workspaces;

    public WorkbenchPage(Element header, Element element) {
        super(header, element);
        workspaces = new Tabs<WorkspacePage, Element>(element.findBy(css(".workspaces"))) {
        };
    }

    public Element DAL() {
        return region.findBy(placeholder("DAL expression"));
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

    public boolean isConnected() {
        return !getHeader().findAllBy(css(".session-state.connected")).isEmpty();
    }

    public void Release() {
        region.findBy(css(".release")).click();
    }

    public void Pass() {
        region.findBy(css(".pass")).click();
    }

    public void newWorkspace() {
        region.findBy(css(".new")).click();
    }

    public WorkspacePage Workspace(String target) {
        return target.equals("Current") ? workspaces.getCurrent() : workspaces.switchTo(target);
    }
}
