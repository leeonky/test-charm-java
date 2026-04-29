package org.testcharm.cucumber.extensions;

import io.cucumber.core.runtime.Remote;
import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.event.*;
import org.testcharm.cucumber.WorkerForwardingPluginExtension;

public class BuildInEventPublisher implements WorkerForwardingPluginExtension {
    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestCaseStarted.class, event -> Remote.REMOTE.sendEvent(event));
        eventPublisher.registerHandlerFor(TestCaseFinished.class, event -> Remote.REMOTE.sendEvent(event));
        eventPublisher.registerHandlerFor(TestStepStarted.class, event -> Remote.REMOTE.sendEvent(event));
        eventPublisher.registerHandlerFor(TestStepFinished.class, event -> Remote.REMOTE.sendEvent(event));
        eventPublisher.registerHandlerFor(Envelope.class, event -> Remote.REMOTE.sendEvent(event));
    }
}
