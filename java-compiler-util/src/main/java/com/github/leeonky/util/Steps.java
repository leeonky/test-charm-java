package com.github.leeonky.util;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class Steps {

    @Before
    public void reset() {
        JavaExecutor.executor().reset();
    }

    @Given("the following bean definition:")
    public void the_following_bean_definition(String sourceCode) {
        JavaExecutor.executor().addClass(sourceCode);
    }

    @When("evaluating the following code:")
    public void executingTheFollowingCode(String expression) {
        JavaExecutor.executor().main().returnExpression(expression);
    }
}
