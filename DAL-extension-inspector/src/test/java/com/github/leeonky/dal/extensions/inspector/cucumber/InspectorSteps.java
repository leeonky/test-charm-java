package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.binary.util.HexFormatter;
import com.github.leeonky.dal.extensions.inspector.Inspector;
import com.github.leeonky.dal.extensions.inspector.InspectorExtension;
import com.github.leeonky.dal.extensions.inspector.cucumber.page.MainPage;
import com.github.leeonky.dal.extensions.inspector.cucumber.pagebk.MainPageBk;
import com.github.leeonky.dal.extensions.inspector.cucumber.pagebk.SeleniumWebDriver;
import com.github.leeonky.interpreter.InterpreterException;
import com.github.leeonky.util.Sneaky;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

import static com.github.leeonky.dal.Assertions.expect;
import static com.github.leeonky.dal.extensions.basic.text.Methods.json;

public class InspectorSteps {
    private TestContext testContext;
    private MainPageBk mainPageBk;
    private MainPage mainPage;
    private final DAL dal = DAL.create(InspectorExtension.class);

    private final SeleniumWebDriver seleniumWebDriver = new SeleniumWebDriver(() ->
            Sneaky.get(() -> new RemoteWebDriver(new URL("http://www.s.com:4444"), DesiredCapabilities.chrome())));

    @After
    public void close() {
        seleniumWebDriver.destroy();
    }

    @Before
    public void initTest() {
        testContext = new TestContext();
        Inspector.shutdown();
    }

    @When("launch inspector web server")
    public void launchInspectorWebServer() {
        Inspector.launch();
        Inspector.ready();
    }

    @And("launch inspector web page")
    public void launchInspectorWebPage() {
        mainPageBk = new MainPageBk(seleniumWebDriver);
        mainPage = new MainPage(new Element(seleniumWebDriver.getWebDriver().findElement(By.tagName("html"))).byCss("body"));
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
            dal.evaluateAll(mainPageBk, expression);
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

    @Then("you should see2:")
    @Then("you2:")
    public void youShouldSee(String expression) {
        try {
            dal.evaluateAll(mainPage, expression);
        } catch (InterpreterException e) {
            throw new AssertionError("\n" + e.show(expression) + "\n\n" + e.getMessage());
        }
    }

    @SneakyThrows
    @Then("you should see2 after {int}s:")
    public void youShouldSee2AfterS(int seconds, String expression) {
        Thread.sleep(seconds * 1000L);
        youShouldSee(expression);
    }
}
