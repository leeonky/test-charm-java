package com.github.leeonky.interpreter;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

public class StringSteps {
    private StringWithPosition stringWithPosition;

    @Given("the string content:")
    public void the_string_content(String content) {
        stringWithPosition = new StringWithPosition(content);
    }

    @When("mark an char position on {int}")
    public void markAnCharPositionOn(int position) {
        stringWithPosition.position(position);
    }

    @Then("got marked string content:")
    public void gotMarkedStringContent(String mark) {
        assertThat("\n" + stringWithPosition.result()).isEqualTo("\n" + mark);
    }

    @When("mark line position on {int}")
    public void markLinePositionOnPosition(int position) {
        stringWithPosition.row(position);
    }

    @When("mark column on {int}")
    public void markColumnOn(int position) {
        stringWithPosition.column(position);
    }

    @Then("got marked string content with prefix length {int}:")
    public void gotMarkedStringContentWithPrefixLength(int length, String mark) {
        assertThat("\n" + stringWithPosition.result(length)).isEqualTo("\n" + mark);
    }
}
