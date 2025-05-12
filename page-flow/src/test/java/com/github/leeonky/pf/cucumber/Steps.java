package com.github.leeonky.pf.cucumber;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.DALException;
import com.github.leeonky.pf.Element;
import com.github.leeonky.util.Sneaky;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.microsoft.playwright.BrowserType;
import de.neuland.pug4j.Pug4J;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
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

public class Steps {
    private Throwable lastError;
    private Javalin javalin;
    private final Selenium.BrowserSelenium browserSelenium = new Selenium.BrowserSelenium(() ->
            Sneaky.get(() -> new RemoteWebDriver(new URL("http://www.s.com:4444"), DesiredCapabilities.chrome())));

    private final Playwright.BrowserPlaywright browserPlaywright = new Playwright.BrowserPlaywright(() -> com.github.leeonky.pf.cucumber.Playwright.playwright.chromium().connect("ws://www.s.com:3000/", new BrowserType.ConnectOptions().setHeaders(
            new HashMap<String, String>() {{
                put("x-playwright-launch-options", "{ \"headless\": false }");
            }})).newContext());
    private Selenium.SeleniumE seleniumE;
    private Playwright.PlaywrightE playwrightE;

    @When("launch the following web page:")
    public void launchTheFollowingWebPage(String pug) throws IOException {
        CountDownLatch serverReadyLatch = new CountDownLatch(1);
        javalin = Javalin.create().events(event -> event.serverStarted(serverReadyLatch::countDown));
        String html = Pug4J.render(new StringReader(pug), "", new HashMap<>());
        javalin.get("/", ctx -> ctx.html(html));
        javalin.start(10081);
    }

    @After
    public void closeAll() {
        if (javalin != null) {
            javalin.close();
            javalin = null;
        }
        browserSelenium.destroy();
        browserPlaywright.destroy();
        lastError = null;
        seleniumE = null;
        playwrightE = null;
    }


    @Then("page in driver selenium should:")
    public void pageInDriverSeleniumShould(String expression) {
        expect(rootSeleniumElement()).should(expression);
    }

    private Selenium.SeleniumE rootSeleniumElement() {
        if (seleniumE == null)
            seleniumE = browserSelenium.open("http://host.docker.internal:10081");
        return seleniumE;
    }

    @Then("page in driver playwright should:")
    public void pageInDriverPlaywrightShould(String expression) {
        expect(rootPlaywrightElement()).should(expression);
    }

    private Playwright.PlaywrightE rootPlaywrightElement() {
        if (playwrightE == null)
            playwrightE = browserPlaywright.open("http://host.docker.internal:10081");
        return playwrightE;
    }

    private final TestLogger logger = TestLoggerFactory.getTestLogger(Element.class);

    @Before
    public void flushLog() {
        logger.clearAll();
    }

    @And("logs should:")
    public void logsShould(String expression) {
        expect(logger.getAllLoggingEvents()).should(expression);
    }

    @When("try to find element via driver playwright:")
    public void findElementViaDriverPlaywright(String expression) {
        try {
            DAL.dal().evaluate(rootPlaywrightElement(), expression);
        } catch (Throwable e) {
            lastError = e;
        }
    }

    @When("try to find element via driver selenium:")
    public void findElementViaDriverSelenium(String expression) {
        try {
            DAL.dal().evaluate(rootSeleniumElement(), expression);
        } catch (Throwable e) {
            lastError = e;
        }
    }

    @Then("failed with:")
    public void failedWith(String message) {
        expect(lastError.getMessage()).isEqualTo(message);
    }

    @When("perform via driver selenium:")
    public void perform_via_driver_selenium(String actions) {
        try {
            DAL.dal().evaluateAll(rootSeleniumElement(), actions);
        } catch (DALException e) {
            String detailMessage = "\n" + e.show(actions) + "\n\n" + e.getMessage();
            throw new AssertionError(detailMessage);
        }
    }

    @When("perform via driver playwright:")
    public void perform_via_driver_playwright(String actions) {
        try {
            DAL.dal().evaluateAll(rootPlaywrightElement(), actions);
        } catch (DALException e) {
            String detailMessage = "\n" + e.show(actions) + "\n\n" + e.getMessage();
            throw new AssertionError(detailMessage);
        }
    }
}
