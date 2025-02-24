package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import org.openqa.selenium.WebElement;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class SeleniumWebElement {
    protected final SeleniumWebDriver driver;
    protected final WebElement element;

    public SeleniumWebElement(SeleniumWebDriver driver, WebElement element) {
        this.element = element;
        this.driver = driver;
    }

    public void click() {
        element.click();
    }

    public String getText() {
        if (element.getTagName().equals("textarea"))
            return element.getAttribute("value");
        return element.getText();
    }

    public void clear() {
        element.clear();
    }

    public void sendKeys(String text) {
        element.sendKeys(text);
    }

    public List<SeleniumWebElement> findAll(By by) {
        return element.findElements(driver.getBy(by)).stream()
                .map(element -> new SeleniumWebElement(driver, element)).collect(toList());
    }

    public String getAttribute(String name) {
        return element.getAttribute(name);
    }
}
