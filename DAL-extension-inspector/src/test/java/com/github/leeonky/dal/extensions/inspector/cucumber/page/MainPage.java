package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.Element;
import com.github.leeonky.dal.extensions.inspector.cucumber.Tabs;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Page;

import static com.github.leeonky.dal.extensions.inspector.cucumber.ui.By.css;

public class MainPage extends Page<Element> {

    private final Tabs<WorkbenchPage, Element> tabs;

    public MainPage(Element element) {
        super(element);
        tabs = new Tabs<WorkbenchPage, Element>(element.findBy(css(".workbenches"))) {
        };
    }

    public Element AutoExecute() {
        return region.findBy(css(".auto-execute.switch"));
    }

    public Element title() {
        return region.findBy(css(".main-title"));
    }

    public WorkbenchPage WorkBench(String name) {
        if ("Current".contains(name))
            return tabs.getCurrent();

        return tabs.switchTo(name);
    }
}
