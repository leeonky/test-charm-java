package org.testcharm.cucumber.extensions;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.event.*;
import org.testcharm.cucumber.swarm.WorkerForwardingPluginExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.testcharm.cucumber.swarm.worker.Remote.REMOTE;

public class BuildInEventPublisher implements WorkerForwardingPluginExtension {
    private final Logger log = LoggerFactory.getLogger(BuildInEventPublisher.class);

    private Set<Class<?>> forwardingEventNames = new HashSet<>(Arrays.asList(
            TestCaseStarted.class,
            TestCaseFinished.class,
            TestStepStarted.class,
            TestStepFinished.class
    ));

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(Event.class, event -> {
            if (forwardingEventNames.contains(event.getClass()))
                REMOTE.sendEvent(event);
            else
                log.info(() -> "ignore event forwarding: " + event.getClass().getName());
        });

        eventPublisher.registerHandlerFor(Envelope.class, event -> {
            if (event.getTestCase().isPresent()) {
                REMOTE.sendEvent(event.getTestCase().get());
                return;
            }
            if (event.getTestCaseStarted().isPresent()) {
                REMOTE.sendEvent(event.getTestCaseStarted().get());
                return;
            }
            if (!(event.getTestRunFinished().isPresent() || event.getTestRunStarted().isPresent()))
                REMOTE.sendEventDeprecated(event);
            else
                log.info(() -> "ignore envelop forwarding: " + event);
        });
    }
}
