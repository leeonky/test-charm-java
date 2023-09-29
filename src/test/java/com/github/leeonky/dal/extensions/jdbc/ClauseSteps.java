package com.github.leeonky.dal.extensions.jdbc;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static com.github.leeonky.dal.Assertions.expect;

public class ClauseSteps {
    Clause clause;

    @Given("clause")
    public void clause(String clause) {
        this.clause = new Clause(clause);
    }

    @Then("clause should be:")
    public void clauseShouldBe(String expression) {
        expect(clause).should(expression);
    }
}
