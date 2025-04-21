package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Element;
import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Tab;
import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Tabs;
import com.github.leeonky.pf.Elements;

import static com.github.leeonky.pf.By.css;

public class WorkbenchRegion extends Tab {

    private final Tabs<WorkspaceRegion, Element> workspaces;

    public WorkbenchRegion(Element header, Element element) {
        super(header, element);
        workspaces = new Tabs<WorkspaceRegion, Element>(locate("css[.workspaces]").single()) {
        };
    }

    public Elements<Element> DAL() {
        return locate("placeholder[DAL expression]");
    }

    public OutputRegion Current() {
        return workspaces.getCurrent().Current();
    }

    public OutputRegion Output(String name) {
        return workspaces.getCurrent().Output(name);
    }

    public void execute() {
        workspaces.getCurrent().execute();
    }

    public boolean isConnected() {
        return !getHeader().findAllBy(css(".session-state.connected")).isEmpty();
    }

    public void Release() {
        perform("css[.release].click");
    }

    public void Pass() {
        perform("css[.pass].click");
    }

    public void newWorkspace() {
        perform("css[.new].click");
    }

    public WorkspaceRegion Workspace(String target) {
        return target.equals("Current") ? workspaces.getCurrent() : workspaces.switchTo(target);
    }
}
