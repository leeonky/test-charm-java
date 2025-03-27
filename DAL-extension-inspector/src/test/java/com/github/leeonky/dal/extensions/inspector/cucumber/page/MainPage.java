package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.InspectorElement;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Page;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Pages;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Target;

import static java.lang.String.format;

public class MainPage extends Page<InspectorElement> {

    private final Pages<WorkbenchPage> remotes = new Pages<WorkbenchPage>() {
        @Override
        public WorkbenchPage getCurrent() {
            return new WorkbenchPage(
                    element.byXpath(".//div[" + containsClass("tab-content") + " and  " + containsClass("active") + " and not(ancestor::div[" + containsClass("tab-content") + "])]"),
                    element.byXpath(".//div[" + containsClass("tab-header") + " and " + containsClass("active") + " and not(ancestor::div[" + containsClass("tab-content") + "])]")
            );
        }

        private String containsClass(String singleClassName) {
            return "contains(concat(' ', normalize-space(@class), ' '), ' " + singleClassName + " ')";
        }
    };

    public MainPage(InspectorElement element) {
        super(element);
    }

    public InspectorElement title() {
        return element.byCss(".main-title");
    }

    public WorkbenchPage WorkBench(String name) {
        if ("Current".contains(name))
            return remotes.getCurrent();

        return remotes.switchTo(new Target<WorkbenchPage>() {
            @Override
            public WorkbenchPage create() {
                return new WorkbenchPage(
                        element.byCss(format(".tab-content[target='%s']", name)),
                        element.byCss(format(".tab-header[target='%s']", name))
                );
            }

            @Override
            public void navigateTo() {
                element.byCss(".work-bench-headers").byText(name).click();
            }
        });
    }
}
