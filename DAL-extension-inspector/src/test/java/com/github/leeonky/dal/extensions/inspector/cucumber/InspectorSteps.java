package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.DAL;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import lombok.SneakyThrows;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openqa.selenium.By.xpath;

public class InspectorSteps {
    private WebDriver webDriver = null;

    @SneakyThrows
    private WebDriver createWebDriver() {
        return new RemoteWebDriver(new URL("http://www.s.com:4444"), DesiredCapabilities.chrome());
    }

    public WebDriver getWebDriver() {
        if (webDriver == null)
            webDriver = createWebDriver();
        return webDriver;
    }

    @Before("@inspector")
    public void launch() {
        DAL.getInstance();
        getWebDriver().get("http://host.docker.internal:10081");
    }

    @After
    public void close() {
        if (webDriver != null) {
            webDriver.quit();
            webDriver = null;
        }
    }

    @Then("you can see page {string}")
    public void youCanSeePage(String text) {
        assertThat(getWebDriver().findElements(xpath("//*[text()='" + text + "']"))).isNotEmpty();
    }
}
