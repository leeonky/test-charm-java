package org.testcharm.cucumber.builder;

import io.cucumber.plugin.event.Result;

import java.time.Duration;

public class EventResultBuilder implements Builder<Result> {
    public io.cucumber.plugin.event.Status status;
    public Duration duration;
    public Throwable error;

    @Override
    public Result build() {
        return new Result(status, duration, error);
    }
}
