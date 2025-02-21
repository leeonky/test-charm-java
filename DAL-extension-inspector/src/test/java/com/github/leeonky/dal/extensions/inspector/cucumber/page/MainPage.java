package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import java.util.List;

public class MainPage {
    private final Panel panel;
    private final SubPageSwitcher<Object> remotes = new SubPageSwitcher();

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

    public TryPage TryIt() {
        return remotes.switchTo(new Target<TryPage>() {
            @Override
            public TryPage create() {
                return new TryPage(panel.byCss(".work-bench-try"));
            }

            @Override
            public void navigateTo() {
                panel.byText("Try It!").click();
            }
        });
    }
}
