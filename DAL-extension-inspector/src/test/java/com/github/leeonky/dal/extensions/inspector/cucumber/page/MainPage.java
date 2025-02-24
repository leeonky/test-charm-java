package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import java.util.List;

import static java.lang.String.format;

public class MainPage {
    private final Panel panel;
    private final PageContainer<WorkbenchPage> remotes = new PageContainer<>();

    public MainPage(SeleniumWebDriver driver) {
        driver.open("http://host.docker.internal:10081");
        panel = new Panel(driver.findAll(new By("css", "body")).get(0));
    }

    public Panel title() {
        return panel.byCss(".main-title");
    }

    public List<Panel> instances() {
        return panel.allByCss(".instance-monitors .switch");
    }

    public WorkbenchPage WorkBench(String name) {
        return remotes.switchTo(new Target<WorkbenchPage>() {
            @Override
            public WorkbenchPage create() {
                return new WorkbenchPage(panel.byCss(format(".tab-content[target='%s']", name)));
            }

            @Override
            public void navigateTo() {
                panel.byCss(".work-bench-headers").byText(name).click();
            }
        });
    }
}
