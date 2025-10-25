package com.github.leeonky.util;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class Steps {

    @Given("the following bean definition:")
    public void the_following_bean_definition(String sourceCode) {
        JavaExecutor.executor().addClass(sourceCode);
    }

    @When("evaluating the following code:")
    public void executingTheFollowingCode(String expression) {
        JavaExecutor.executor().setValueEvaluator(expression);
    }
}
