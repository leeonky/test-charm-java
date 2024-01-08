package com.github.leeonky.dal.extensions.basic.sync;

import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.extensions.basic.TimeUtil;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.DataRemarkParameterAcceptor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.util.Suppressor;

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
        do {
            try {
                return context.calculate(data, operator, v2);
            } catch (RuntimeException e) {
                exception = e;
                Suppressor.run(() -> Thread.sleep(interval));
            }
        } while (times-- > 0);
        throw exception;
    }

    public DataRemarkParameterAcceptor<Eventually> within() {
        return s -> new Eventually(data, interval, TimeUtil.parseTime(s));
    }

    public DataRemarkParameterAcceptor<Eventually> interval() {
        return s -> new Eventually(data, TimeUtil.parseTime(s), waitingTime);
    }
}
