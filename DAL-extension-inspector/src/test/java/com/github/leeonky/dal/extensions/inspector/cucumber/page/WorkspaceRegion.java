package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Element;
import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Tab;
import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Tabs;

import static com.github.leeonky.dal.uiat.By.css;
import static com.github.leeonky.dal.uiat.By.placeholder;

public class WorkspaceRegion extends Tab {
    private final Tabs<OutputRegion, Element> outputs;

    public WorkspaceRegion(Element header, Element element) {
        super(header, element);
        outputs = new Tabs<OutputRegion, Element>(element.findBy(css(".code-results"))) {

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
        element.findBy(css(".run")).click();
    }

    public Element DAL() {
        return element.findBy(placeholder("DAL expression"));
    }

    public void newWorkspace() {
        element.findBy(css(".new")).click();
    }

    public void dismiss() {
        element.findBy(css(".dismiss")).click();
    }
}
