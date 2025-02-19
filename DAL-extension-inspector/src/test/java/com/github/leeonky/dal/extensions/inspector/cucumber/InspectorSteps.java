package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.inspector.Inspector;
import com.github.leeonky.dal.extensions.inspector.InspectorExtension;
import com.github.leeonky.dal.extensions.inspector.cucumber.page.Browser;
import com.github.leeonky.dal.extensions.inspector.cucumber.page.MainPage;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static com.github.leeonky.dal.Assertions.expect;
import static com.github.leeonky.dal.extensions.basic.text.Methods.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class InspectorSteps {
    private final Browser browser = new Browser();
    private TestContext testContext;
    private MainPage mainPage;
    private DAL dal = DAL.create(InspectorExtension.class);

    @After
    public void close() {
        browser.quit();
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
        mainPage = browser.launch();
    }

    @And("shutdown web server")
    public void shutdownWebServer() {
        Inspector.shutdown();
    }

    @Given("created DAL {string} with inspector extended")
    public void createdDALInsWithInspectorExtended(String name) {
        DAL.create(name);
    }

    @When("try dal on page:")
    public void tryDalOnPage(String expression) {
        browser.byText("Try").click();
        browser.byPlaceholder("DAL expression").fillIn(expression);
    }

    @Then("yon can see the {string}:")
    public void yonCanSeeTheResult(String type, String text) {
        browser.byText("Try").click();
        browser.byText(type).click();
        await().ignoreExceptions().untilAsserted(() ->
                assertThat(browser.byPlaceholder(type).text()).isEqualTo(text));
    }

    @Then("yon can see the Error:")
    public void yonCanSeeTheError(String text) {
        browser.byText("Try").click();
        browser.byText("Error").click();
        await().ignoreExceptions().untilAsserted(() ->
                assertThat(browser.byPlaceholder("Error").text()).isEqualTo(text));
    }

    @And("you can see the Inspect:")
    public void youCanSeeTheInspect(String text) {
        browser.byText("Try").click();
        browser.byText("Inspect").click();
        await().ignoreExceptions().untilAsserted(() ->
                assertThat(browser.byPlaceholder("Inspect").text()).isEqualTo(text));
    }

    @When("given default input value:")
    public void givenDefaultInputValue(String json) {
        Inspector.setDefaultInput(() -> json(json));
    }

    @When("append try dal on page:")
    public void appendTryDalOnPage(String code) {
        browser.byText("Try").click();
        browser.byPlaceholder("DAL expression").typeIn(code);
    }

    @Then("you should see:")
    public void youShouldSee(String expression) {
        expect(mainPage).use(dal).should(expression);
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
