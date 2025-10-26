package com.github.leeonky.util;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class Steps {

    @Before
    public void reset() {
        JavaExecutor.executor().reset();
    }

    @Given("the following bean definition:")
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
    public void registerAsFollows(String registers) {
        JavaExecutor.executor().main().addRegisters(registers);
    }
}
