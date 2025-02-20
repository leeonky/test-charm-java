package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import org.openqa.selenium.WebElement;

public class SeleniumWebElement {
    private final WebElement element;

    public SeleniumWebElement(WebElement element) {
        this.element = element;
    }

    public void click() {
        element.click();
    }

    public String getText() {
        return element.getText();
    }

    public void clear() {
        element.clear();
    }

    public void sendKeys(String text) {
        element.sendKeys(text);
    }
}
