package io.cucumber.core.runtime;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.messages.types.Envelope;

public class Remote {
    public static Remote REMOTE;

    public static void setupRemote(Server server) {
        REMOTE = new Remote(new Client(server));
    }

    private final Worker worker = new Worker();
    private final Client client;

    public Remote(Client client) {
        this.client = client;
    }

    public Pickle requestPickle() {
        return client.requestPickle(worker);
    }

    public boolean register() {
        return client.register(worker);
    }

    //TODO test: should not forward worker testRunFinished and testRunStarted
    public void sendEvent(Object event) {
        if (event instanceof Envelope) {
            if (!(((Envelope) event).getTestRunFinished().isPresent() || ((Envelope) event).getTestRunStarted().isPresent()))
                client.sendEvent(worker, event);
        } else
            client.sendEvent(worker, event);
    }
}
