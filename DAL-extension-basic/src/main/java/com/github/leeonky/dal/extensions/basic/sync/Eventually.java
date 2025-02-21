package com.github.leeonky.dal.extensions.basic.sync;

import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.extensions.basic.TimeUtil;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.util.Suppressor;

import java.time.Duration;
import java.time.Instant;

public class Eventually {
    private static int defaultWaitingTime = 5000;
    private final Data data;
    private final int interval;
    private final int waitingTime;

    public Eventually(Data data) {
        this(data, 100, defaultWaitingTime);
    }

    public Eventually(Data data, int interval, int waitingTime) {
        this.data = data;
        this.interval = interval;
        this.waitingTime = waitingTime;
    }

    public static void setDefaultWaitingTime(int defaultWaitingTime) {
        Eventually.defaultWaitingTime = defaultWaitingTime;
    }

    public Data verify(DALOperator operator, Data v2, RuntimeContextBuilder.DALRuntimeContext context) {
        RuntimeException exception;
        int times = waitingTime / interval;
        Instant now = Instant.now();
        do {
            times--;
            try {
                return context.calculate(data, operator, v2);
            } catch (RuntimeException e) {
                exception = e;
                if (times > 0)
                    Suppressor.run(() -> Thread.sleep(interval));
            }
        } while (times > 0 && Duration.between(now, Instant.now()).toMillis() < waitingTime);
        throw exception;
    }

    public Eventually within(String s) {
        return new Eventually(data, interval, TimeUtil.parseTime(s));
    }

    public Eventually interval(String s) {
        return new Eventually(data, TimeUtil.parseTime(s), waitingTime);
    }
}
