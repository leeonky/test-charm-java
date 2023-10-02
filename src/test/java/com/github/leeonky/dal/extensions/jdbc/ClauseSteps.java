package com.github.leeonky.dal.extensions.jdbc;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static com.github.leeonky.dal.Assertions.expect;

public class ClauseSteps {
    ClauseParser clauseParser;

    @Given("clause")
    public void clause(String clause) {
        clauseParser = new ClauseParser(clause);
    }

    @Then("clause should be:")
    public void clauseShouldBe(String expression) {
        expect(clauseParser).should(expression);
    }
}
