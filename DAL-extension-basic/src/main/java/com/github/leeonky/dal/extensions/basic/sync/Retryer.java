package com.github.leeonky.dal.extensions.basic.sync;

import com.github.leeonky.util.Suppressor;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

public class Retryer {
    private final int waitingTime;
    private final int interval;

    public Retryer(int waitingTime, int interval) {
        this.waitingTime = waitingTime;
        this.interval = interval;
    }

    public <T> T get(Supplier<T> s) throws Throwable {
        Throwable exception;
        Instant start = Instant.now();
        do {
            try {
                return s.get();
            } catch (Throwable e) {
                exception = e;
            }
        } while (timeout(start) && sleep());
        throw exception;
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
