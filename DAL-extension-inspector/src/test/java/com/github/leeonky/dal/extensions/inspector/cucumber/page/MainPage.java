package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Element;
import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Tabs;
import com.github.leeonky.pf.Elements;
import com.github.leeonky.pf.Region;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MainPage extends Region<Element> {

    private final Tabs<WorkbenchRegion, Element> tabs;

    public MainPage(Element element) {
        super(element);
        tabs = new Tabs<WorkbenchRegion, Element>(locate("css[.workbenches]").single()) {
        };
    }

    public Elements<Element> AutoExecute() {
        return locate("css[.auto-execute.switch]");
    }

    public Elements<Element> title() {
        return locate("css[.main-title]");
    }

    public WorkbenchRegion WorkBench(String name) {
        if ("Current".contains(name))
            return tabs.getCurrent();

        return tabs.switchTo(name);
    }

    public Map<String, Element> Monitors() {
        return locate("css[.instance-monitors .switch]").stream()
                .collect(Collectors.toMap(Element::text, Function.identity()));
    }

    public void Release(String workbenchName) {
        WorkBench(workbenchName).Release();
    }

    public void ReleaseAll() {
        perform("css[.release.release-all].click");
    }

    public void Pass(String workbenchName) {
        WorkBench(workbenchName).Pass();
    }
}
