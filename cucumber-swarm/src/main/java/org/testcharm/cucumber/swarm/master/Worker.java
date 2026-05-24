package org.testcharm.cucumber.swarm.master;

import java.util.concurrent.atomic.AtomicInteger;

public interface Worker {
    AtomicInteger ID_GENERATOR = new AtomicInteger();

    int id();

    void shutdown();

    void ready();

    boolean isReady();
}
