package org.testcharm.dal.extensions.inspector.cucumber.page;

import org.testcharm.dal.extensions.inspector.cucumber.page.e.Element;
import org.testcharm.dal.extensions.inspector.cucumber.page.e.Tab;
import org.testcharm.dal.extensions.inspector.cucumber.page.e.Tabs;
import org.testcharm.pf.Elements;

public class WorkspacePanel extends Tab {
    private final Tabs<OutputPanel, Element> outputs;

    public WorkspacePanel(Element header, Element element) {
        super(header, element);
        outputs = new Tabs<OutputPanel, Element>(locate("css[.code-results]").single()) {

            @Override
            public OutputPanel getCurrent() {
                try {
                    return createTab(locate("patience[2s].css[.tab-header.active]").single(),
                            locate("css[.tab-content.active]").single());
                } catch (Exception ignore) {
                    return null;
                }
            }

            @Override
            protected OutputPanel createTab(Element header, Element tab) {
                if (header.text().equals("Watches"))
                    return new WatchesPanel(header, tab);
                return new OutputPanel(header, tab);
            }
        };
    }

    public OutputPanel Current() {
        return outputs.getCurrent();
    }

    public OutputPanel Output(String name) {
        return outputs.switchTo(name);
    }

    public void execute() {
        perform("css[.run].click");
    }

    public Elements<Element> DAL() {
        return locate("placeholder[DAL expression]");
    }

    public void newWorkspace() {
        perform("css[.new].click");
    }

    public void dismiss() {
        perform("css[.dismiss].click");
    }
}
