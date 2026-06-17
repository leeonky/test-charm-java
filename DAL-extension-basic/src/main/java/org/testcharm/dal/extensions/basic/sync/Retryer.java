package org.testcharm.dal.extensions.basic.sync;

import org.testcharm.util.Sneaky;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

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

    public <T> T get(Supplier<T> s) {
        Throwable exception;
        Instant start = Instant.now();
        do {
            try {
                return CompletableFuture.supplyAsync(s).get(Math.max(waitingTime * 2, defaultTimeout()), MILLISECONDS);
            } catch (ExecutionException e) {
                exception = e.getCause();
            } catch (Throwable e) {
                exception = e;
            }
        } while (timeout(start));
        return Sneaky.sneakyThrow(exception);
    }

    private boolean timeout(Instant now) {
        if (Duration.between(now, Instant.now()).toMillis() < waitingTime) {
            Sneaky.run(() -> Thread.sleep(interval));
            return true;
        }
        return false;
    }
}
