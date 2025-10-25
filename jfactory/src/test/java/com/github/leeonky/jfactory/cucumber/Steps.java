package com.github.leeonky.jfactory.cucumber;

import com.github.leeonky.util.JavaExecutor;
import io.cucumber.java.en.Then;

import static com.github.leeonky.dal.Assertions.expectRun;

public class Steps {
    @Then("the result should be:")
    public void the_result_should_be(String expression) {
        expectRun(JavaExecutor.executor()::evaluate).should(expression);
    }
}
