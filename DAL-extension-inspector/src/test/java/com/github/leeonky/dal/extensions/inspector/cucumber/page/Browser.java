package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

import static org.openqa.selenium.By.xpath;

public class Browser {
    private WebDriver webDriver = null;

    @SneakyThrows
    private WebDriver createWebDriver() {
        return new RemoteWebDriver(new URL("http://www.s.com:4444"), DesiredCapabilities.chrome());
    }

    private WebDriver getWebDriver() {
        if (webDriver == null)
            webDriver = createWebDriver();
        return webDriver;
    }

    public void quit() {
        if (getWebDriver() != null) {
            getWebDriver().quit();
            webDriver = null;
        }
    }

    public MainPage launch() {
        getWebDriver().get("http://host.docker.internal:10081");
        return new MainPage(this);
    }

    public FindingBy byText(String text) {
        return by(xpath(String.format("//*[normalize-space(@value)='%s' or normalize-space(text())='%s']", text, text)));
    }

    public FindingBy byCss(String cssSelector) {
        return by(By.cssSelector(cssSelector));
    }

    public FindingBy by(By by) {
        return new FindingBy(by, getWebDriver());
    }

    public FindingBy byPlaceholder(String dalExpression) {
        return by(xpath(String.format("//*[@placeholder='%s']", dalExpression)));
    }
}