package com.github.leeonky.jfactory.cucumber;

import com.github.leeonky.util.JavaExecutor;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static com.github.leeonky.dal.Assertions.expectRun;

public class Steps {

    @Before
    public void importDependencies() {
        JavaExecutor.executor().main().importDependency("com.github.leeonky.jfactory.*");
    }

    @Then("the result should be:")
    public void the_result_should_be(String expression) {
        expectRun(JavaExecutor.executor().main()::evaluate).should(expression);
    }

    @Then("value of {string} should be:")
    public void value_of_should_be(String field, String expression) {
        JavaExecutor.executor().main().returnExpression(field);
        expectRun(JavaExecutor.executor().main()::evaluate).should(expression);
    }

    @Given("the following spec definition:")
    public void theFollowingSpecDefinition(String sourceCode) {
        JavaExecutor.executor().addClass("import com.github.leeonky.jfactory.Spec;\n" + sourceCode);
    }
}
