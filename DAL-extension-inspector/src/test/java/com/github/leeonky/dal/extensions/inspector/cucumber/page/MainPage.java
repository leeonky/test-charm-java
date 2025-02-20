package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import java.util.List;

public class MainPage {
    private final Panel panel;
    private final SubPageSwitcher remotes;

    public MainPage(SeleniumWebDriver driver) {
        driver.open("http://host.docker.internal:10081");
        panel = new Panel(driver.findAll(new By("css", "body")).get(0));
        remotes = new SubPageSwitcher();
    }

    public Panel title() {
        return panel.byCss(".main-title");
    }

    public List<Panel> instances() {
        return panel.allByCss(".instance-monitors .switch");
    }

    public TryPage TryIt() {
        return remotes.switchTo(() -> panel.byText("Try").click(), () -> new TryPage(panel), TryPage.class
        );
    }
}
