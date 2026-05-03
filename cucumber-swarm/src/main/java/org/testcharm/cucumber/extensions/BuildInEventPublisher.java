package org.testcharm.cucumber.extensions;

import io.cucumber.plugin.event.EventPublisher;
import org.testcharm.cucumber.swarm.WorkerForwardingPluginExtension;

public class BuildInEventPublisher implements WorkerForwardingPluginExtension {
    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
//        eventPublisher.registerHandlerFor(TestCaseStarted.class, event -> Remote.REMOTE.sendEvent(event));
//        eventPublisher.registerHandlerFor(TestCaseFinished.class, event -> Remote.REMOTE.sendEvent(event));
//        eventPublisher.registerHandlerFor(TestStepStarted.class, event -> Remote.REMOTE.sendEvent(event));
//        eventPublisher.registerHandlerFor(TestStepFinished.class, event -> Remote.REMOTE.sendEvent(event));
//        eventPublisher.registerHandlerFor(Envelope.class, event -> {
//            if (!(event.getTestRunFinished().isPresent() || event.getTestRunStarted().isPresent()))
//                Remote.REMOTE.sendEvent(event);
//        });
    }
}
