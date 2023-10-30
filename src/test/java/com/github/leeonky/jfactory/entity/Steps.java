package com.github.leeonky.jfactory.entity;

import com.github.leeonky.jfactory.DALHelper;
import com.github.leeonky.jfactory.JFactory;
import com.github.leeonky.jfactory.Spec;
import com.github.leeonky.util.Classes;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static com.github.leeonky.dal.Assertions.expect;

public class Steps {
    Context context = new Context();

    @Before
    public void clear() {
        context = new Context();
    }

    @When("create {string}:")
    public void create(String traitSpec, String dal) {
        context.createOne(traitSpec, dal);
    }

    @Then("all {string} should:")
    public void all_should(String spec, String content) {
        context.shouldBe(spec, content);
    }

    @When("try to create {string}:")
    public void tryToCreate(String traitSpec, String dal) {
        context.tryToCreate(traitSpec, dal);
    }

    @Then("got following exception:")
    public void gotFollowingException(String expression) {
        context.shouldRaise(expression);
    }

    @When("create some {string}:")
    public void createSome(String traitSpec, String dal) {
        context.createSome(traitSpec, dal);
    }

    public static class Context {
        private final JFactory jFactory = new JFactory() {{
            Classes.assignableTypesOf(Spec.class, "com.github.leeonky.jfactory.specs").forEach(this::register);
        }};

        private Exception e;

        public void createOne(String traitSpec, String dal) {
            jFactory.spec(traitSpec.split(" ")).properties(DALHelper.given(dal)).create();
        }

        public void createSome(String traitSpec, String dal) {
            Object object = DALHelper.dalParse(dal);
            ((List<DALHelper.ObjectValue>) object).forEach(objectValue ->
                    jFactory.spec(traitSpec.split(" ")).properties(objectValue.flat()).create());
        }

        public void shouldBe(String spec, String content) {
            expect(jFactory.spec(spec).queryAll()).should(content);
        }

        public void tryToCreate(String traitSpec, String dal) {
            try {
                createOne(traitSpec, dal);
            } catch (Exception e) {
                this.e = e;
            }
        }

        public void shouldRaise(String expression) {
            expect(e).should(expression);
        }
    }
}
