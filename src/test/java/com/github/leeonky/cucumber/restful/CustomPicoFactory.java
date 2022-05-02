package com.github.leeonky.cucumber.restful;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.picocontainer.PicoFactory;

import java.util.function.Consumer;

public class CustomPicoFactory implements ObjectFactory {
    private final PicoFactory delegate = new PicoFactory();
    private RestfulStep restfulStep;
    private Steps steps;

    @Override
    public void start() {
        delegate.start();
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public boolean addClass(Class<?> glueClass) {
        return delegate.addClass(glueClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> glueClass) {
        if (glueClass.equals(RestfulStep.class)) {
            return (T) getRestfulStep();
        }
        if (glueClass.equals(Steps.class)) {
            return (T) getSteps();
        }
        return delegate.getInstance(glueClass);
    }

    private Steps getSteps() {
        if (steps == null)
            steps = new Steps(getInstance(RestfulStep.class));
        return steps;
    }

    private RestfulStep getRestfulStep() {
        if (restfulStep == null)
            restfulStep = new RestfulStep();
        return restfulStep;
    }

    public static Consumer<String> lookupAction = s -> {
    };
}
