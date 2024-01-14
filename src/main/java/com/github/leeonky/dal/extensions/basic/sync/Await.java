package com.github.leeonky.dal.extensions.basic.sync;

import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.extensions.basic.TimeUtil;
import com.github.leeonky.dal.extensions.basic.list.NotReadyException;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.DataRemarkParameterAcceptor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.util.Suppressor;

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

    public Data await(DALOperator operator, Data v2, DALRuntimeContext context) {
        RuntimeException exception;
        int times = waitingTime / interval;
        do {
            times--;
            try {
                return context.calculate(data, operator, v2);
            } catch (NotReadyException e) {
                exception = e;
                if (times > 0)
                    Suppressor.run(() -> Thread.sleep(interval));
            }
        } while (times > 0);
        throw exception;
    }

    public Await within(String s) {
        return new Await(data, interval, TimeUtil.parseTime(s));
    }

    public DataRemarkParameterAcceptor<Await> interval() {
        return s -> new Await(data, TimeUtil.parseTime(s), waitingTime);
    }
}
