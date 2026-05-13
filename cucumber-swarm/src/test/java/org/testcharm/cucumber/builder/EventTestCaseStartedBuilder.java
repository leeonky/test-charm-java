package org.testcharm.cucumber.builder;

import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseStarted;

import java.time.Instant;

public class EventTestCaseStartedBuilder implements Builder {
    public Instant instant;
    public TestCase testCase;

    @Override
    public TestCaseStarted build() {
        return new TestCaseStarted(instant, testCase);
    }
}
