package io.cucumber.core.runtime;

import io.cucumber.core.gherkin.Pickle;

public class Client {
    private final Server server;

    public Client(Server server) {
        this.server = server;
    }

    public Pickle requestPickle(Worker worker) {
        return server.responsePickle(worker);
    }

    public boolean register(Worker worker) {
        return server.register(worker);
    }

    public void sendEvent(Worker worker, Object event) {
        server.receiveEvent(worker, event);
    }
}
