package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import java.util.List;

public class MainPage {
    private final Panel panel;
    private final SubPageSwitcher remotes;

    public MainPage(SeleniumWebDriver driver) {
        driver.open("http://host.docker.internal:10081");
        panel = new Panel(driver);
        remotes = new SubPageSwitcher(panel);
    }

    public SeleniumWebElement title() {
        return panel.byCss(".main-title").locate();
    }

    public List<SeleniumWebElement> instances() {
        return panel.byCss(".instance-monitors .switch").findAll();
    }

    public TryPage TryIt() {
        return remotes.switchTo(() -> panel.byText("Try").click(), () -> new TryPage(panel), TryPage.class
        );
    }
}
