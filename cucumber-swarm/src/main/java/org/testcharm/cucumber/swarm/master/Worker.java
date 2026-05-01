package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.gherkin.Pickle;
import org.testcharm.util.Sneaky;

import java.lang.reflect.Proxy;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Worker {
    static final AtomicInteger ID_GENERATOR = new AtomicInteger();
    private final int id = ID_GENERATOR.getAndIncrement();

    public static final Pickle NO_PICKLE = (Pickle) Proxy.newProxyInstance(Pickle.class.getClassLoader(), new Class[]{Pickle.class}, (proxy, method, args) -> {
        throw new IllegalStateException("Should not call " + method.getName() + " on NO_PICKLE");
    });
    private final SynchronousQueue<Pickle> queue = new SynchronousQueue<>();

    public void responseNewPickle(Pickle pickle) {
        Sneaky.run(() -> queue.put(pickle));
    }

    public Pickle requestPickle() {
        return Sneaky.get(queue::take);
    }

    public void exit() {
        responseNewPickle(NO_PICKLE);
    }

    public int id() {
        return id;
    }
}
