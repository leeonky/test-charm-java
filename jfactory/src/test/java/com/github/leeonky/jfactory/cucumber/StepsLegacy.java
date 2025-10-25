package com.github.leeonky.jfactory.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

@Deprecated
public class StepsLegacy {
    private IntegrationTestContextLegacy integrationTestContextLecacy;

    @Before
    public void reset() {
        integrationTestContextLecacy = new IntegrationTestContextLegacy();
    }

    @After
    public void releaseCompiler() {
        integrationTestContextLecacy.releaseCompiler();
    }

    @Given("the following bean class:")
    public void the_following_bean_class(String classCode) {
        integrationTestContextLecacy.givenBean(classCode);
    }

    @Given("the following spec class:")
    public void the_following_spec_class(String specClass) {
        integrationTestContextLecacy.specClass(specClass);
    }

    @Given("declaration jFactory =")
    public void declarationJFactory(String declaration) {
        integrationTestContextLecacy.declare(declaration);
    }

    @Given("declaration list =")
    public void declarationList(String listDeclaration) {
        integrationTestContextLecacy.declareList(listDeclaration);
    }

    @Then("the list in repo should:")
    public void theListInRepoShould(String dal) {
        integrationTestContextLecacy.listShould(dal);
    }

    @And("register:")
    public void register(String factorySnippet) {
        integrationTestContextLecacy.register(factorySnippet);
    }

    @And("operate:")
    public void operate(String operateSnippet) {
        integrationTestContextLecacy.register(operateSnippet);
    }

    @When("build:")
    public void build(String builderSnippet) {
        integrationTestContextLecacy.build(builderSnippet);
    }

    @Then("{string} should")
    public void should(String code, String dal) throws Throwable {
        integrationTestContextLecacy.build(code + ";");
        integrationTestContextLecacy.verify(dal);
    }

    @Then("the result should:")
    public void the_result_should(String dal) throws Throwable {
        integrationTestContextLecacy.verify(dal);
    }

    @Then("should raise error:")
    public void shouldRaiseError(String dal) {
        integrationTestContextLecacy.shouldThrow(dal);
    }
}
