package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.inspector.Inspector;
import com.github.leeonky.dal.extensions.inspector.InspectorExtension;
import com.github.leeonky.dal.extensions.inspector.cucumber.page.MainPage;
import com.github.leeonky.dal.extensions.inspector.cucumber.page.SeleniumWebDriver;
import com.github.leeonky.interpreter.InterpreterException;
import com.github.leeonky.util.Suppressor;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

import static com.github.leeonky.dal.extensions.basic.text.Methods.json;

public class InspectorSteps {
    private TestContext testContext;
    private MainPage mainPage;
    private final DAL dal = DAL.create(InspectorExtension.class);

    private final SeleniumWebDriver seleniumWebDriver = new SeleniumWebDriver(() ->
            Suppressor.get(() -> new RemoteWebDriver(new URL("http://www.s.com:4444"), DesiredCapabilities.chrome())));

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
    }

    @And("launch inspector web page")
    public void launchInspectorWebPage() {
        mainPage = new MainPage(seleniumWebDriver);
    }

    @And("shutdown web server")
    public void shutdownWebServer() {
        Inspector.shutdown();
    }

    @Given("created DAL {string} with inspector extended")
    public void createdDALInsWithInspectorExtended(String name) {
        DAL.create(name);
    }

    @When("given default input value:")
    public void givenDefaultInputValue(String json) {
        Inspector.setDefaultInput(() -> json(json));
    }

    @When("you:")
    @Then("you should see:")
    public void you(String expression) {
        try {
            dal.evaluateAll(mainPage, expression);
        } catch (InterpreterException e) {
            throw new AssertionError("\n" + e.show(expression) + "\n\n" + e.getMessage());
        }
    }

    //    @SneakyThrows
//    @And("restart inspector")
//    public void resetInspectorServer() {
//        Inspector.shutdown();
//        Inspector.launch();
//        Inspector.getInstances().clear();
//        Thread.sleep(1000);
//    }
//
//    @Before("@inspector")
//    @When("launch inspector")
//    public void launch() {
//        getWebDriver().get("http://host.docker.internal:10081");
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
//    @Before
//    public void initTest() {
//        Inspector.shutdown();
//        Inspector.getInstances().clear();
//        Inspector.launch();
//        testContext = new TestContext();
//    }
//
//    @When("DAL inspector is in mode {string}")
//    public void dalInspectorIsInMode(String mode) {
//        testContext.changeInspectorMode(mode);
//    }
//
//    @Given("the following data:")
//    public void theFollowingData(String jsonData) {
//        testContext.givenData(json(jsonData));
//    }
//
//    @When("evaluating the following:")
//    public void evaluatingTheFollowing(String expression) {
//        testContext.evaluating(expression);
//    }
//
//    @Then("test is still running after {int}s")
//    public void testIsStillRunningAfterS(int second) {
//        testContext.shouldStillRunningAfter(second * 1000);
//    }
//
//    @Then("should display the same DAL expression")
//    public void shouldDisplayTheSameDALExpression() {
//        await().ignoreExceptions().untilAsserted(() ->
//                assertThat(getWebDriver().findElement(xpath("//*[@placeholder='DAL expression']")).getAttribute("value"))
//                        .isEqualTo(testContext.lastEvaluating()));
//    }
//
//    @And("should show the following result:")
//    public void shouldShowTheFollowingResult(String result) {
//        await().ignoreExceptions().untilAsserted(() ->
//                expect(getWebDriver().findElement(xpath("//*[@placeholder='Result']")).getText())
//                        .use(DAL.create(InspectorExtension.class))
//                        .isEqualTo(result));
//    }
//
//    @And("should show the following error:")
//    public void shouldShowTheFollowingError(String result) {
//        await().ignoreExceptions().untilAsserted(() ->
//                expect(getWebDriver().findElement(xpath("//*[@placeholder='Error']")).getText())
//                        .use(DAL.create(InspectorExtension.class))
//                        .isEqualTo(result));
//    }
//
//    @Then("test failed with error:")
//    public void testFailedWithError(String error) {
//        testContext.shouldFailedWith(error);
//    }
//
//    @Then("should display DAL expression:")
//    public void shouldDisplayDALExpression(String dalExpression) {
//        await().ignoreExceptions().untilAsserted(() ->
//                expect(getWebDriver().findElement(xpath("//*[@placeholder='DAL expression']")).getAttribute("value"))
//                        .use(DAL.create(InspectorExtension.class))
//                        .isEqualTo(dalExpression));
//    }
//
//    @When("resume suspended")
//    public void resumeSuspended() {
//        String text = "Resume";
//        await().ignoreExceptions().untilAsserted(() -> getWebDriver().findElement(
//                xpath(String.format("//*[normalize-space(@value)='%s' or normalize-space(text())='%s']", text, text))).click());
//    }
//
//    @When("evaluating the following by another DAL {string}:")
//    public void evaluatingTheFollowingByAnotherDAL(String name, String expression) {
//        testContext.evaluatingAnother(name, expression);
//    }
//
//    @Then("you can see the DAL instances:")
//    public void youCanSeeTheDALInstances(DataTable instances) {
//        await().ignoreExceptions().untilAsserted(() ->
//        {
//            expect(getWebDriver().findElements(cssSelector(".dal-instance")).stream().map(WebElement::getText).collect(Collectors.toList()))
//                    .use(DAL.create(InspectorExtension.class))
//                    .isEqualTo(instances.asList());
//        });
//    }
//
//    @And("current DAL instance is {string}")
//    public void currentDALInstanceIs(String value) {
//        await().ignoreExceptions().untilAsserted(() ->
//                expect(getWebDriver().findElement(cssSelector(".dal-session-current")).getText())
//                        .use(DAL.create(InspectorExtension.class))
//                        .isEqualTo(value));
//    }
//
//    @When("not check {string}")
//    public void notCheckTest(String text) {
//        await().ignoreExceptions().untilAsserted(() -> getWebDriver().findElement(
//                xpath(String.format("//*[normalize-space(@value)='%s' or normalize-space(text())='%s']", text, text))).click());
//    }
}
