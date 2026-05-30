package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

public abstract class AbstractWorker implements Worker {
    private static final Logger log = LoggerFactory.getLogger(Worker.class);
    private boolean ready = false;

    @Override
    public void ready() {
        ready = true;
        log.info(() -> String.format("Worker<%d> is ready", id()));
    }

    @Override
    public boolean isReady() {
        return ready;
    }
}
