package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.binary.util.HexFormatter;
import com.github.leeonky.dal.extensions.inspector.Inspector;
import com.github.leeonky.dal.extensions.inspector.InspectorExtension;
import com.github.leeonky.dal.extensions.inspector.cucumber.page.MainPage;
import com.github.leeonky.interpreter.InterpreterException;
import com.github.leeonky.pf.PageFlow;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;

import java.util.HashMap;

import static com.github.leeonky.dal.Assertions.expect;
import static com.github.leeonky.dal.extensions.basic.text.Methods.json;
import static com.github.leeonky.pf.By.css;

public class InspectorSteps {
    private TestContext testContext;
    private MainPage mainPage;
    private final DAL dal = DAL.create(InspectorExtension.class);

//    private final BrowserSelenium browser = new BrowserSelenium(() ->
//            Sneaky.get(() -> new RemoteWebDriver(new URL("http://www.s.com:4444"), DesiredCapabilities.chrome())));

    private static final Playwright playwright = Playwright.create();
    private final BrowserPlaywright browser = new BrowserPlaywright(() -> playwright.chromium().connect("ws://www.s.com:3000/", new BrowserType.ConnectOptions().setHeaders(
            new HashMap<String, String>() {{
                put("x-playwright-launch-options", "{ \"headless\": false }");
            }})));


    @After
    public void close() {
        browser.destroy();
    }

    @Before
    public void initTest() {
        testContext = new TestContext();
        Inspector.shutdown();
        PageFlow.setDAL(dal);
    }

    @When("launch inspector web server")
    public void launchInspectorWebServer() {
        Inspector.launch();
        Inspector.ready();
    }

    @And("launch inspector web page")
    public void launchInspectorWebPage() {
        mainPage = new MainPage(browser.open("http://host.docker.internal:10082").find(css("body")).single());
    }

    @And("shutdown web server")
    public void shutdownWebServer() {
        Inspector.shutdown();
    }

    @Given("created DAL {string} with inspector extended")
    public void createdDALInsWithInspectorExtended(String name) {
        testContext.createDAL(name);
    }

    @When("given default input value:")
    public void givenDefaultInputValue(String json) {
        Inspector.setDefaultInput(() -> json(json));
    }

    @When("you:")
    @Then("you should see:")
    @Deprecated
    public void you(String expression) {
        try {
            dal.evaluateAll(mainPage, expression);
        } catch (InterpreterException e) {
            throw new AssertionError("\n" + e.show(expression) + "\n\n" + e.getMessage());
        }
    }

    @And("the {string} following input:")
    public void theInsFollowingInput(String dalIns, String inputJson) {
        testContext.addInput(dalIns, json(inputJson));
    }

    @When("use DAL {string} to evaluating the following:")
    public void useDALInsToEvaluatingTheFollowing(String dalIns, String code) {
        testContext.evaluate(dalIns, code);
    }

    @Then("{string} test still run after {float}s")
    public void insTestStillRunAfterS(String dalIns, float second) {
        testContext.shouldStillRunningAfter(dalIns, second);
    }

    @Then("DAL {string} test finished with the following result")
    public void dalInsTestFinishedWithTheFollowingResult(String dalIns, String result) {
        expect(testContext.resultOf(dalIns)).use(dal).should(result);
    }

    @Given("Inspector in {string} mode")
    public void inspectorInAUTOMode(String mode) {
        Inspector.setDefaultMode(Inspector.Mode.valueOf(mode));
    }

    @SneakyThrows
    @Then("you should see after {int}s:")
    public void youShouldSeeAfterS(int seconds, String expression) {
        Thread.sleep(seconds * 1000L);
        you(expression);
    }

    @Given("the {string} binary input:")
    public void theInsBinaryInput(String dalIns, String binary) {
        testContext.addInput(dalIns, new HexFormatter().format(binary, null));
    }
}
