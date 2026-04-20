package org.testcharm.jfactory.legacy2;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testcharm.jfactory.JFactory;
import org.testcharm.jfactory.JFactoryDAL;
import org.testcharm.jfactory.Spec;
import org.testcharm.util.Classes;

import static org.testcharm.dal.Assertions.expect;

public class Steps {
    Context context = new Context();

    @Before
    public void clear() {
        context = new Context();
    }

    @When("create {string}:")
    public void create(String traitSpec, String dal) {
        context.create(traitSpec, dal);
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
        context.create(traitSpec, dal);
    }

    @When("create")
    public void create(String dal) {
        context.create(dal);
    }

    public static class Context {
        private final JFactory jFactory = new JFactory() {{
            Classes.assignableTypesOf(Spec.class, "org.testcharm.jfactory.legacy2.specs").forEach(this::register);
        }};

        private final JFactoryDAL jFactoryDAL = new JFactoryDAL(jFactory);

        private Exception e;

        public void create(String traitSpec, String dal) {
            jFactoryDAL.create(traitSpec, dal);
        }

        public void shouldBe(String spec, String content) {
            expect(jFactory.spec(spec).queryAll()).should(content);
        }

        public void tryToCreate(String traitSpec, String dal) {
            try {
                create(traitSpec, dal);
            } catch (Exception e) {
                this.e = e;
            }
        }

        public void shouldRaise(String expression) {
            expect(e).should(expression);
        }

        public void create(String dal) {
            jFactoryDAL.createAll(dal);
        }
    }
}
