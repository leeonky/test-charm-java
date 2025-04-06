package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Element;
import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Tabs;
import com.github.leeonky.pf.Region;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.leeonky.pf.By.css;

public class MainPage extends Region<Element> {

    private final Tabs<WorkbenchRegion, Element> tabs;

    public MainPage(Element element) {
        super(element);
        tabs = new Tabs<WorkbenchRegion, Element>(element.findBy(css(".workbenches"))) {
        };
    }

    public Element AutoExecute() {
        return element.findBy(css(".auto-execute.switch"));
    }

    public Element title() {
        return element.findBy(css(".main-title"));
    }

    public WorkbenchRegion WorkBench(String name) {
        if ("Current".contains(name))
            return tabs.getCurrent();

        return tabs.switchTo(name);
    }

    public Map<String, Element> Monitors() {
        return element.findAllBy(css(".instance-monitors .switch")).stream()
                .collect(Collectors.toMap(Element::text, Function.identity()));
    }

    public void Release(String workbenchName) {
        WorkBench(workbenchName).Release();
    }

    public void ReleaseAll() {
        element.findBy(css(".release.release-all")).click();
    }

    public void Pass(String workbenchName) {
        WorkBench(workbenchName).Pass();
    }
}
