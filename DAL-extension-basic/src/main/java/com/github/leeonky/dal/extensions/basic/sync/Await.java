package com.github.leeonky.dal.extensions.basic.sync;

import com.github.leeonky.dal.extensions.basic.TimeUtil;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.util.Suppressor;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.function.Function;

public class Await {
    private static int defaultWaitingTime = 5000;
    private final Data data;
    private final int interval;
    private final int waitingTime;

    public Await(Data data) {
        this(data, 100, defaultWaitingTime);
    }

    public Await(Data data, int interval, int waitingTime) {
        this.data = data;
        this.interval = interval;
        this.waitingTime = waitingTime;
    }

    public static void setDefaultWaitingTime(int ms) {
        Await.defaultWaitingTime = ms;
    }

    public <T> T await(Function<Data, T> supplier) throws Exception {
        Exception exception;
        int times = waitingTime / interval;
        Instant now = Instant.now();
        do {
            times--;
            try {
                return supplier.apply(data);
            } catch (Exception e) {
                exception = e;
                if (times > 0)
                    Suppressor.run(() -> Thread.sleep(interval));
            }
        } while (times > 0 && Duration.between(now, Instant.now()).toMillis() < waitingTime);
        throw exception;
    }

    public Await within(String s) {
        return new Await(data, interval, TimeUtil.parseTime(s));
    }

    public Await interval(String s) {
        return new Await(data, TimeUtil.parseTime(s), waitingTime);
    }

    public Set<Object> fieldNames() {
        return data.fieldNames();
    }
}
