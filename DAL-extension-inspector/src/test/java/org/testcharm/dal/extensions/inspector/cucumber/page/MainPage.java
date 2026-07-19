package org.testcharm.dal.extensions.inspector.cucumber.page;

import org.testcharm.dal.extensions.inspector.cucumber.page.e.Element;
import org.testcharm.dal.extensions.inspector.cucumber.page.e.Tabs;
import org.testcharm.dal.runtime.SoloList;
import org.testcharm.pf.AbstractPanel;
import org.testcharm.pf.Elements;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MainPage extends AbstractPanel<Element> {

    private final Tabs<WorkbenchPanel, Element> tabs;

    public MainPage(Element element) {
        super(element);
        tabs = new Tabs<WorkbenchPanel, Element>(locate("css[.workbenches]").single()) {
        };
    }

    public Elements<Element> AutoExecute() {
        return locate("css[.auto-execute.switch]");
    }

    public Elements<Element> title() {
        return locate("css[.main-title]");
    }

    public WorkbenchPanel WorkBench(String name) {
        if ("Current".contains(name))
            return tabs.getCurrent();

        return tabs.switchTo(name);
    }

    public Map<String, Element> Monitors() {
        return ((SoloList<Element>) locate("css[.instance-monitors .switch]")).list().values()
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
