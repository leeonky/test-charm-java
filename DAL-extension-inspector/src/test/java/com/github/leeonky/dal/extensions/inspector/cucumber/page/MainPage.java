package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import org.openqa.selenium.WebElement;

import java.util.List;

public class MainPage {
    private final Browser browser;
    private final ViewFrame inspectingPanel;

    public MainPage(Browser browser) {
        this.browser = browser;
        inspectingPanel = new ViewFrame(browser);
    }

    public WebElement title() {
        return browser.byCss(".main-title").locate();
    }

    public List<WebElement> instances() {
        return browser.byCss(".instance-monitors .switch").findAll();
    }

    public TryPage tryIt() {
        return inspectingPanel.switchTo(() -> browser.byText("Try").click(), () -> new TryPage(browser), TryPage.class
        );
    }
}
