package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Element;
import org.openqa.selenium.WebDriver;

import java.util.Objects;
import java.util.function.Supplier;

public class BrowserSelenium {
    private final Supplier<WebDriver> driverFactory;
    private WebDriver webDriver;

    public BrowserSelenium(Supplier<WebDriver> driverFactory) {
        this.driverFactory = Objects.requireNonNull(driverFactory);
    }

    public void destroy() {
        if (webDriver != null) {
            webDriver.quit();
            webDriver = null;
        }
    }

    public Element open(String url) {
        return null;
//        if (webDriver == null)
//            webDriver = driverFactory.get();
//        webDriver.get(url);
//        return new Element(webDriver.findElement(By.tagName("html")));
    }
}