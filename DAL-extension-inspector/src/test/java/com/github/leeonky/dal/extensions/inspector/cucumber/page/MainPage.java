package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import org.openqa.selenium.WebElement;

import java.util.List;

public class MainPage {
    private final Browser browser;

    public MainPage(Browser browser) {
        this.browser = browser;
    }

    public WebElement title() {
        return browser.byCss(".main-title").locate();
    }

    public List<WebElement> instances() {
        return browser.byCss(".instance-monitors .switch").findAll();
    }
}
