package org.testcharm.cucumber.swarm;

import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import org.testcharm.cucumber.swarm.worker.WorkerDataMapper;
import org.testcharm.util.Classes;

import static java.util.stream.Stream.concat;
import static org.testcharm.util.Classes.subTypesOf;

public class WorkerForwardingPlugin implements ConcurrentEventListener {

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        concat(subTypesOf(WorkerForwardingPluginExtension.class, "org.testcharm.cucumber.extensions").stream(),
                subTypesOf(WorkerForwardingPluginExtension.class, "org.testcharm.extensions.cucumber").stream())
                .map(Classes::newInstance)
                .forEach(e -> e.setEventPublisher(eventPublisher));

        eventPublisher.registerHandlerFor(Envelope.class, envelop -> {
            envelop.getStepDefinition().ifPresent(stepDefinition -> WorkerDataMapper.instance().mapStepDefinition(stepDefinition));
            envelop.getHook().ifPresent(hook -> WorkerDataMapper.instance().mapHook(hook));
        });
    }
}
