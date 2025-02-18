package com.github.leeonky.dal.extensions.inspector.cucumber;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.leeonky.util.function.Extension.not;
import static org.awaitility.Awaitility.await;

public class FindingBy {
    private final By by;
    private final WebDriver webDriver;

    public FindingBy(By by, WebDriver webDriver) {
        this.by = by;
        this.webDriver = webDriver;
    }

    public WebElement locate() {
        List<WebElement> list = await().ignoreExceptions().until(this::findAll, not(List::isEmpty));
        if (list.size() > 1)
            throw new IllegalStateException("Found more than one elements: " + by.toString());
        return list.get(0);
    }

    private List<WebElement> findAll() {
        return webDriver.findElements(by);
    }

    public void click() {
        locate().click();
    }

    public String text() {
        return locate().getText();
    }

    public List<String> eachText() {
        return findAll().stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public void input(String text) {
        locate().sendKeys(text);
    }
}
