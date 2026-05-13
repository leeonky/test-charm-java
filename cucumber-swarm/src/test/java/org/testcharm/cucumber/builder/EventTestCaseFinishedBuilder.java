package org.testcharm.cucumber.builder;

import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;

import java.time.Instant;

public class EventTestCaseFinishedBuilder implements Builder<TestCaseFinished> {
    public Instant instant;
    public EventResultBuilder result;
    public TestCase testCase;

    @Override
    public TestCaseFinished build() {
        return new TestCaseFinished(instant, testCase, result.build());
    }
}
