package org.testcharm.cucumber.extensions;

import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.event.*;
import org.testcharm.cucumber.swarm.WorkerForwardingPluginExtension;

import static org.testcharm.cucumber.swarm.worker.Remote.REMOTE;

public class BuildInEventPublisher implements WorkerForwardingPluginExtension {
    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestCaseStarted.class, event -> REMOTE.sendEvent(event));
        eventPublisher.registerHandlerFor(TestCaseFinished.class, event -> REMOTE.sendEvent(event));
        eventPublisher.registerHandlerFor(TestStepStarted.class, event -> REMOTE.sendEvent(event));
        eventPublisher.registerHandlerFor(TestStepFinished.class, event -> REMOTE.sendEvent(event));
        eventPublisher.registerHandlerFor(Envelope.class, event -> {
            if (!(event.getTestRunFinished().isPresent() || event.getTestRunStarted().isPresent()))
                REMOTE.sendEvent(event);
        });
    }
}
