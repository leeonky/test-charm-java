package com.github.leeonky.dal.extensions.basic.sync;

import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.extensions.basic.TimeUtil;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

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

    public Data verify(DALOperator operator, Data v2, RuntimeContextBuilder.DALRuntimeContext context) throws Throwable {
        return new Retryer(waitingTime, interval).get(() -> context.calculate(data, operator, v2));
    }

    public Eventually within(String s) {
        return new Eventually(data, interval, TimeUtil.parseTime(s));
    }

    public Eventually interval(String s) {
        return new Eventually(data, TimeUtil.parseTime(s), waitingTime);
    }
}
