package org.testcharm.cucumber.swarm.worker;

import io.cucumber.core.gherkin.Pickle;
import org.testcharm.cucumber.swarm.master.Server;

import java.util.Optional;

public class Client {
    private final Server server;

    public Client(Server server) {
        this.server = server;
    }

    public Pickle requestPickle(int workerId) {
        return server.responsePickle(workerId);
    }

    public Optional<Integer> register() {
        return server.register();
    }

    public void sendEvent(int workerId, Object event) {
        server.receiveEvent(workerId, event);
    }
}
