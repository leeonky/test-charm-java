package com.github.leeonky.dal.extensions.basic.sync;

import com.github.leeonky.util.Suppressor;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static com.github.leeonky.dal.runtime.RuntimeException.extractException;

public class Retryer {
    private static int defaultTimeout = 0;
    private final int waitingTime;
    private final int interval;

    public Retryer(int waitingTime, int interval) {
        this.waitingTime = waitingTime;
        this.interval = interval;
    }

    public static void setDefaultTimeout(int ms) {
        defaultTimeout = ms;
    }

    public static int defaultTimeout() {
        return defaultTimeout;
    }

    public <T> T get(Supplier<T> s) throws Throwable {
        Throwable exception;
        Instant start = Instant.now();
        do {
            try {
                return CompletableFuture.supplyAsync(s).get(Math.max(waitingTime, defaultTimeout),
                        java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                exception = e.getCause();
            } catch (Throwable e) {
                exception = e;
            }
        } while (timeout(start) && sleep());
        throw extractException(exception).orElse(exception);
    }

    private boolean timeout(Instant now) {
        return Duration.between(now, Instant.now()).toMillis() < waitingTime;
    }

    private boolean sleep() {
        Suppressor.run(() -> Thread.sleep(interval));
        return true;
    }

    public void run(Runnable runnable) throws Throwable {
        get(() -> {
            runnable.run();
            return null;
        });
    }
}
