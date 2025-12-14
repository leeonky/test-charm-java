package com.github.leeonky.util;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static com.github.leeonky.dal.Assertions.expectRun;

public class Steps {

    @Before(order = 0)
    public void reset() {
        JavaExecutor.executor().reset();
    }

    @Given("the following bean definition:")
    @Given("the following class definition:")
    public void theFollowingBeanDefinition(String sourceCode) {
        JavaExecutor.executor().addClass(sourceCode);
    }

    @When("evaluating the following code:")
    public void executingTheFollowingCode(String expression) {
        JavaExecutor.executor().main().returnExpression(expression);
    }

    @Given("the following declarations:")
    public void theFollowingDeclarations(String declarations) {
        JavaExecutor.executor().main().addDeclarations(declarations);
    }

    @And("register as follows:")
    @And("execute as follows:")
    public void registerAsFollows(String registers) {
        JavaExecutor.executor().main().addRegisters(registers);
    }

    @Before(order = 1)
    public void importDependency(Scenario scenario) {
        scenario.getSourceTagNames().stream().filter(s -> s.startsWith("@import"))
                .forEach(s -> JavaExecutor.executor().main().importDependency(s.replace("@import(", "").replace(")", "")));
    }

    @Then("the result should be:")
    public void the_result_should_be(String expression) {
        expectRun(JavaExecutor.executor().main()::evaluate).should(expression);
    }

    @Then("the field {string} should be:")
    public void value_of_should_be(String field, String expression) {
        JavaExecutor.executor().main().returnExpression(field);
        expectRun(JavaExecutor.executor().main()::evaluate).should(expression);
    }
}
