package org.testcharm.cucumber.swarm;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import org.testcharm.cucumber.swarm.worker.WorkerDataMapper;
import org.testcharm.util.Classes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Stream.concat;
import static org.testcharm.cucumber.swarm.worker.Remote.REMOTE;
import static org.testcharm.util.Classes.subTypesOf;

public class WorkerForwardingPlugin implements ConcurrentEventListener {

    private final Logger log = LoggerFactory.getLogger(WorkerForwardingPlugin.class);

    private static final Set<Class<?>> forwardingEventNames = new HashSet<>(Arrays.asList(
            TestCaseStarted.class,
            TestCaseFinished.class,
            TestStepStarted.class,
            TestStepFinished.class
    ));

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(Envelope.class, envelop -> {
            envelop.getStepDefinition().ifPresent(stepDefinition -> WorkerDataMapper.instance().mapStepDefinition(stepDefinition));
            envelop.getHook().ifPresent(hook -> WorkerDataMapper.instance().mapHook(hook));
            envelop.getTestCase().ifPresent(testCase -> WorkerDataMapper.instance().mapTestCase(testCase));
        });

        eventPublisher.registerHandlerFor(Event.class, event -> {
            if (forwardingEventNames.contains(event.getClass()))
                REMOTE.sendEvent(event);
            else
                log.debug(() -> "ignore event forwarding: " + event.getClass().getName());
        });

        eventPublisher.registerHandlerFor(Envelope.class, event -> {
            if (event.getTestCase().isPresent()) {
                REMOTE.sendEvent(event.getTestCase().get());
            } else if (event.getTestCaseStarted().isPresent()) {
                REMOTE.sendEvent(event.getTestCaseStarted().get());
            } else if (event.getTestStepStarted().isPresent()) {
                REMOTE.sendEvent(event.getTestStepStarted().get());
            } else if (event.getTestStepFinished().isPresent()) {
                REMOTE.sendEvent(event.getTestStepFinished().get());
            } else if (event.getTestCaseFinished().isPresent()) {
                REMOTE.sendEvent(event.getTestCaseFinished().get());
            }

            concat(subTypesOf(WorkerForwardingPluginExtension.class, "org.testcharm.cucumber.extensions").stream(),
                    subTypesOf(WorkerForwardingPluginExtension.class, "org.testcharm.extensions.cucumber").stream())
                    .map(Classes::newInstance)
                    .forEach(e -> e.setEventPublisher(eventPublisher));
        });
    }
}
