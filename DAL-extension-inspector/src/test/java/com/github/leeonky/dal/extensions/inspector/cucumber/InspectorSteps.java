package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.extensions.inspector.Inspector;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

import static com.github.leeonky.dal.Assertions.expect;
import static com.github.leeonky.dal.extensions.basic.text.Methods.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.openqa.selenium.By.xpath;

public class InspectorSteps {
    private WebDriver webDriver = null;
    private TestContext testContext;

    @SneakyThrows
    private WebDriver createWebDriver() {
        return new RemoteWebDriver(new URL("http://www.s.com:4444"), DesiredCapabilities.chrome());
    }

    public WebDriver getWebDriver() {
        if (webDriver == null)
            webDriver = createWebDriver();
        return webDriver;
    }

    @Before
    public void resetInspectorServer() {
        Inspector.shutdown();
        Inspector.launch();
    }

    @Before("@inspector")
    @When("launch inspector")
    public void launch() {
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

    @Before
    public void initTest() {
        testContext = new TestContext();
    }

    @When("DAL inspector is in mode {string}")
    public void dalInspectorIsInMode(String mode) {
        testContext.changeInspectorMode(mode);
    }

    @Given("the following data:")
    public void theFollowingData(String jsonData) {
        testContext.givenData(json(jsonData));
    }

    @When("evaluating the following:")
    public void evaluatingTheFollowing(String expression) {
        testContext.evaluating(expression);
    }

    @Then("test is still running after {int}s")
    public void testIsStillRunningAfterS(int second) {
        testContext.shouldStillRunningAfter(second * 1000);
    }

    @Then("should display the same DAL expression")
    public void shouldDisplayTheSameDALExpression() {
        await().ignoreExceptions().untilAsserted(() ->
                assertThat(getWebDriver().findElement(xpath("//*[@placeholder='DAL expression']")).getAttribute("value"))
                        .isEqualTo(testContext.lastEvaluating()));
    }

    @And("should show the following result:")
    public void shouldShowTheFollowingResult(String result) {
        await().ignoreExceptions().untilAsserted(() ->
                expect(getWebDriver().findElement(xpath("//*[@placeholder='Result']")).getText())
                        .isEqualTo(result));
    }

    @Then("test failed with error:")
    public void testFailedWithError(String error) {
        testContext.shouldFailedWith(error);
    }

    @Then("should display DAL expression:")
    public void shouldDisplayDALExpression(String dalExpression) {
        await().ignoreExceptions().untilAsserted(() ->
                expect(getWebDriver().findElement(xpath("//*[@placeholder='DAL expression']")).getAttribute("value"))
                        .isEqualTo(dalExpression));
    }
}
