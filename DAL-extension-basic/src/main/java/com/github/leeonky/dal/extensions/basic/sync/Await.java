package com.github.leeonky.dal.extensions.basic.sync;

import com.github.leeonky.dal.extensions.basic.TimeUtil;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.ProxyObject;
import com.github.leeonky.util.Sneaky;

import java.util.Set;
import java.util.function.Function;

import static com.github.leeonky.dal.runtime.DalException.extractException;

public class Await implements ProxyObject {
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

    public <T> T await(Function<Data, T> supplier) {
        try {
            return new Retryer(waitingTime, interval).get(() -> supplier.apply(data));
        } catch (Throwable e) {
            return Sneaky.sneakyThrow(extractException(e).orElse(e));
        }
    }

    public Await within(String s) {
        return new Await(data, interval, TimeUtil.parseTime(s));
    }

    public Await interval(String s) {
        return new Await(data, TimeUtil.parseTime(s), waitingTime);
    }

    @Override
    public Object getValue(Object property) {
        return Sneaky.get(() -> await(data -> data.getValue(property).instance()));
    }

    @Override
    public Set<?> getPropertyNames() {
        return data.resolved().fieldNames();
    }
}
