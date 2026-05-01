package org.testcharm.cucumber.extensions;

import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.event.*;
import org.testcharm.cucumber.swarm.WorkerForwardingPluginExtension;
import org.testcharm.cucumber.swarm.worker.Remote;

public class BuildInEventPublisher implements WorkerForwardingPluginExtension {
    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestCaseStarted.class, event -> Remote.REMOTE.sendEvent(event));
        eventPublisher.registerHandlerFor(TestCaseFinished.class, event -> Remote.REMOTE.sendEvent(event));
        eventPublisher.registerHandlerFor(TestStepStarted.class, event -> Remote.REMOTE.sendEvent(event));
        eventPublisher.registerHandlerFor(TestStepFinished.class, event -> Remote.REMOTE.sendEvent(event));
        eventPublisher.registerHandlerFor(Envelope.class, event -> {
            if (!(event.getTestRunFinished().isPresent() || event.getTestRunStarted().isPresent()))
                Remote.REMOTE.sendEvent(event);
        });
    }
}
