package com.github.leeonky.pf.cucumber;

import com.github.leeonky.util.Sneaky;
import com.microsoft.playwright.BrowserType;
import de.neuland.pug4j.Pug4J;
import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.javalin.Javalin;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static com.github.leeonky.dal.Assertions.expect;
import static com.github.leeonky.pf.By.css;

public class Steps {
    private Javalin javalin;
    private final Selenium.BrowserSelenium browserSelenium = new Selenium.BrowserSelenium(() ->
            Sneaky.get(() -> new RemoteWebDriver(new URL("http://www.s.com:4444"), DesiredCapabilities.chrome())));

    private final Playwright.BrowserPlaywright browserPlaywright = new Playwright.BrowserPlaywright(() -> com.github.leeonky.pf.cucumber.Playwright.playwright.chromium().connect("ws://www.s.com:3000/", new BrowserType.ConnectOptions().setHeaders(
            new HashMap<String, String>() {{
                put("x-playwright-launch-options", "{ \"headless\": false }");
            }})));

    @When("launch the following web page:")
    public void launchTheFollowingWebPage(String pug) throws IOException {
        CountDownLatch serverReadyLatch = new CountDownLatch(1);
        javalin = Javalin.create().events(event -> event.serverStarted(serverReadyLatch::countDown));
        String html = Pug4J.render(new StringReader(pug), "", new HashMap<>());
        javalin.get("/", ctx -> ctx.html(html));
        javalin.start(10081);
    }

    @Then("page in driver selenium should:")
    public void pageInDriverSeleniumShould(String expression) {
        expect(browserSelenium.open("http://host.docker.internal:10081").findBy(css("body")))
                .should(expression);
    }

    @After
    public void closeAll() {
        if (javalin != null) {
            javalin.close();
            javalin = null;
        }
        browserSelenium.destroy();
        browserPlaywright.destroy();
    }

    @Then("page in driver playwright should:")
    public void pageInDriverPlaywrightShould(String expression) {
        expect(browserPlaywright.open("http://host.docker.internal:10081").findBy(css("body")))
                .should(expression);
    }
}
