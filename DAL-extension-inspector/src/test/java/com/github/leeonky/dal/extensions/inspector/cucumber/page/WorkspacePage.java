package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.Element;
import com.github.leeonky.dal.extensions.inspector.cucumber.Tab;
import com.github.leeonky.dal.extensions.inspector.cucumber.Tabs;

import static com.github.leeonky.dal.extensions.inspector.cucumber.ui.By.css;

public class WorkspacePage extends Tab {
    private final Tabs<OutputPage, Element> outputs;

    public WorkspacePage(Element header, Element element) {
        super(header, element);
        outputs = new Tabs<OutputPage, Element>(element.findBy(css(".code-results"))) {

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
}
