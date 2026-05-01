package io.cucumber.core.runtime;

import io.cucumber.core.gherkin.Pickle;

public class Server {
    private final DistributedPickleScheduler scheduler;

    public Server(DistributedPickleScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Pickle responsePickle(Worker worker) {
        return scheduler.responsePickle(worker);
    }

    public boolean register(Worker worker) {
        return scheduler.register(worker);
    }

    public void stop() {
    }

    public void receiveEvent(Worker worker, Object event) {
        scheduler.receiveEvent(worker, event);
    }
}
