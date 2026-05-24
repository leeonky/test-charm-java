package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

public abstract class AbstractWorker implements Worker {
    private final Logger log = LoggerFactory.getLogger(Worker.class);
    protected final int id = Worker.ID_GENERATOR.incrementAndGet();

    private boolean ready = false;

    @Override
    public int id() {
        return id;
    }

    @Override
    public void ready() {
        ready = true;
        log.info(() -> String.format("Worker<%d> is ready", id));
    }

    @Override
    public boolean isReady() {
        return ready;
    }
}
