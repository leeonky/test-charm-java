package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.leeonky.util.function.Extension.not;
import static org.awaitility.Awaitility.await;

public class Locator {
    protected final SeleniumWebDriver seleniumWebDriver;
    protected final By by;

    public Locator(By by, SeleniumWebDriver seleniumWebDriver) {
        this.by = by;
        this.seleniumWebDriver = seleniumWebDriver;
    }

    public List<SeleniumWebElement> findAll() {
        return seleniumWebDriver.findAll(by);
    }

    public SeleniumWebElement locate() {
        List<SeleniumWebElement> list = await().ignoreExceptions().until(this::findAll, not(List::isEmpty));
        if (list.size() > 1)
            throw new IllegalStateException("Found more than one elements: " + by.toString());
        return list.get(0);
    }

    public void click() {
        locate().click();
    }

    public String text() {
        return locate().getText();
    }

    public List<String> eachText() {
        return findAll().stream().map(SeleniumWebElement::getText).collect(Collectors.toList());
    }

    public void fillIn(String text) {
        SeleniumWebElement locate = locate();
        locate.clear();
        locate.sendKeys(text);
    }

    public void typeIn(String text) {
        locate().sendKeys(text);
    }
}
