package io.cucumber.core.runtime;

import io.cucumber.core.gherkin.Pickle;

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

    public void register() {
        client.register(worker);
    }

    public void sendEvent(Object event) {
        client.sendEvent(worker, event);
    }
}
