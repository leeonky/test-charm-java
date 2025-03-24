package com.github.leeonky.dal.extensions.inspector.cucumber.pagebk;

import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SeleniumWebDriver {
    private final Supplier<WebDriver> driverFactory;
    protected WebDriver webDriver;

    public SeleniumWebDriver(Supplier<WebDriver> driverFactory) {
        this.driverFactory = Objects.requireNonNull(driverFactory);
    }

    protected WebDriver getWebDriver() {
        if (webDriver == null)
            webDriver = driverFactory.get();
        return webDriver;
    }

    public void destroy() {
        if (webDriver != null) {
            webDriver.quit();
            webDriver = null;
        }
    }

    public List<SeleniumWebElement> findAll(By by) {
        return getWebDriver().findElements(Objects.requireNonNull(getBy(by), "Unsupported find " + by)).stream()
                .map(element -> new SeleniumWebElement(this, element)).collect(Collectors.toList());
    }

    public org.openqa.selenium.By getBy(By by) {
        switch (by.type()) {
            case By.XPATH:
                return org.openqa.selenium.By.xpath(Objects.requireNonNull(by.value()).toString());
            case By.CSS:
                return org.openqa.selenium.By.cssSelector(Objects.requireNonNull(by.value()).toString());
            default:
                return null;
        }
    }

    public void open(String path) {
        getWebDriver().get(path);
    }
}