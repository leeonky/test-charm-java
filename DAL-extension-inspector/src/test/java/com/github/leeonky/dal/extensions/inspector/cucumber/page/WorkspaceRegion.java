package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Element;
import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Tab;
import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Tabs;
import com.github.leeonky.pf.Elements;

import static com.github.leeonky.pf.By.css;

public class WorkspaceRegion extends Tab {
    private final Tabs<OutputRegion, Element> outputs;

    public WorkspaceRegion(Element header, Element element) {
        super(header, element);
        outputs = new Tabs<OutputRegion, Element>(locate("css[.code-results]").single()) {

            @Override
            public OutputRegion getCurrent() {
                try {
                    return createTab(element.findAllBy(css(".tab-header.active")).get(0),
                            element.findAllBy(css(".tab-content.active")).get(0));
                } catch (Exception ignore) {
                    return null;
                }
            }

            @Override
            protected OutputRegion createTab(Element header, Element tab) {
                if (header.text().equals("Watches"))
                    return new WatchesRegion(header, tab);
                return new OutputRegion(header, tab);
            }
        };
    }

    public OutputRegion Current() {
        return outputs.getCurrent();
    }

    public OutputRegion Output(String name) {
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
