package com.github.leeonky.pf.cucumber;

import com.github.leeonky.pf.SeleniumElement;
import com.github.leeonky.util.Sneaky;
import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.javalin.Javalin;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import static com.github.leeonky.dal.Assertions.expect;
import static com.github.leeonky.pf.By.css;

public class Steps {
    private Javalin javalin;
    private final BrowserSelenium browserSelenium = new BrowserSelenium(() ->
            Sneaky.get(() -> new RemoteWebDriver(new URL("http://www.s.com:4444"), DesiredCapabilities.chrome())));

    @When("launch the following web page:")
    public void launchTheFollowingWebPage(String html) {
        CountDownLatch serverReadyLatch = new CountDownLatch(1);
        javalin = Javalin.create().events(event -> event.serverStarted(serverReadyLatch::countDown));
        javalin.get("/", ctx -> ctx.html(html));
        javalin.start(10081);
    }

    @Then("page in driver selenium should:")
    public void pageInDriverSeleniumShould(String expression) {
        expect(browserSelenium.open("http://host.docker.internal:10081").findBy(css("body")))
                .should(expression);
    }

    @After
    void closeWebServer() {
        if (javalin != null) {
            javalin.close();
            javalin = null;
        }
        browserSelenium.destroy();
    }

    public static class SeleniumE extends SeleniumElement<SeleniumE> {
        public SeleniumE(WebElement element) {
            super(element);
        }
    }

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

        public SeleniumE open(String url) {
            if (webDriver == null)
                webDriver = driverFactory.get();
            webDriver.get(url);
            return new SeleniumE(webDriver.findElement(By.tagName("html")));
        }
    }
}
