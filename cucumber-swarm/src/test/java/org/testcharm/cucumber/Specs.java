package org.testcharm.cucumber;

import io.cucumber.plugin.event.TestCase;
import org.testcharm.cucumber.builder.EventTestCaseFinishedBuilder;
import org.testcharm.cucumber.builder.EventTestCaseStartedBuilder;
import org.testcharm.jfactory.Spec;
import org.testcharm.util.BeanClass;
import org.testcharm.util.BeanClassProxy;

public class Specs {
    public static class EventTestCase extends Spec<TestCase> {
        @Override
        public BeanClass<TestCase> getType() {
            return BeanClassProxy.create(TestCase.class);
        }
    }

    public static class EventTestCaseStarted extends Spec<EventTestCaseStartedBuilder> {
    }

    public static class EventTestCaseFinished extends Spec<EventTestCaseFinishedBuilder> {
    }
}
