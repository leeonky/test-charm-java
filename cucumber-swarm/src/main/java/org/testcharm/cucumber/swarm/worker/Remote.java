package org.testcharm.cucumber.swarm.worker;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.messages.types.Envelope;
import org.testcharm.cucumber.swarm.master.Server;

public class Remote {
    private static final int NOT_EXIST = -1;

    public static Remote REMOTE;

    public static void setupRemote(Server server) {
        REMOTE = new Remote(new Client(server));
    }

    private int workerId;
    private final Client client;

    public Remote(Client client) {
        this.client = client;
    }

    public Pickle requestPickle() {
        return client.requestPickle(workerId);
    }

    public boolean register() {
        workerId = client.register().orElse(NOT_EXIST);
        return workerId != NOT_EXIST;
    }

    //TODO test: should not forward worker testRunFinished and testRunStarted
    public void sendEvent(Object event) {
        if (event instanceof Envelope) {
            if (!(((Envelope) event).getTestRunFinished().isPresent() || ((Envelope) event).getTestRunStarted().isPresent()))
                client.sendEvent(workerId, event);
        } else
            client.sendEvent(workerId, event);
    }
}
