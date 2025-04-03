package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.Objects;
import java.util.function.Supplier;

public class Browser {
    private final Supplier<WebDriver> driverFactory;
    private WebDriver webDriver;

    public Browser(Supplier<WebDriver> driverFactory) {
        this.driverFactory = Objects.requireNonNull(driverFactory);
    }

    public void destroy() {
        if (webDriver != null) {
            webDriver.quit();
            webDriver = null;
        }
    }

    public Element open(String url) {
        if (webDriver == null)
            webDriver = driverFactory.get();
        webDriver.get(url);
        return new Element(webDriver.findElement(By.tagName("html")));
    }
}