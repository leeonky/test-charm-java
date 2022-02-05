package com.github.leeonky.cucumber.restful;


import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.picocontainer.PicoFactory;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;

public class CustomPicoFactory implements ObjectFactory {
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .dns(CustomPicoFactory::lookup)
            .build();
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

    @NotNull
    private Steps getSteps() {
        if (steps == null)
            steps = new Steps(getInstance(RestfulStep.class));
        return steps;
    }

    @NotNull
    private RestfulStep getRestfulStep() {
        if (restfulStep == null)
            restfulStep = new RestfulStep(HTTP_CLIENT);
        return restfulStep;
    }

    public static Consumer<String> lookupAction = s -> {
    };

    @SneakyThrows
    public static List<InetAddress> lookup(String host) {
        lookupAction.accept(host);
        return singletonList(InetAddress.getLocalHost());
    }
}
