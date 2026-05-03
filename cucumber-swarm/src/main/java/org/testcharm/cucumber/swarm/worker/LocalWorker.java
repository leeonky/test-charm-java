package org.testcharm.cucumber.swarm.worker;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class LocalWorker implements Worker {
    private Logger log = LoggerFactory.getLogger(LocalWorker.class);

    static final AtomicInteger ID_GENERATOR = new AtomicInteger();
    private final int id = ID_GENERATOR.getAndIncrement();

    @Override
    public Integer id() {
        return id;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
