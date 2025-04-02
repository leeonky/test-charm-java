package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.Element;
import com.github.leeonky.dal.extensions.inspector.cucumber.Tab;
import com.github.leeonky.dal.extensions.inspector.cucumber.Tabs;

import static com.github.leeonky.dal.extensions.inspector.cucumber.ui.By.css;
import static com.github.leeonky.dal.extensions.inspector.cucumber.ui.By.placeholder;

public class WorkspacePage extends Tab {
    private final Tabs<OutputPage, Element> outputs;

    public WorkspacePage(Element header, Element element) {
        super(header, element);
        outputs = new Tabs<OutputPage, Element>(element.findBy(css(".code-results"))) {

            @Override
            public OutputPage getCurrent() {
                try {
                    return createTab(region.findAllBy(css(".tab-header.active")).get(0),
                            region.findAllBy(css(".tab-content.active")).get(0));
                } catch (Exception ignore) {
                    return null;
                }
            }

            @Override
            protected OutputPage createTab(Element header, Element tab) {
                if (header.text().equals("Watches"))
                    return new WatchesPage(header, tab);
                return new OutputPage(header, tab);
            }
        };
    }

    public OutputPage Current() {
        return outputs.getCurrent();
    }

    public OutputPage Output(String name) {
        return outputs.switchTo(name);
    }

    public void execute() {
        region.findBy(css(".run")).click();
    }

    public Element DAL() {
        return region.findBy(placeholder("DAL expression"));
    }

    public void newWorkspace() {
        region.findBy(css(".new")).click();
    }

    public void dismiss() {
        region.findBy(css(".dismiss")).click();
    }
}
