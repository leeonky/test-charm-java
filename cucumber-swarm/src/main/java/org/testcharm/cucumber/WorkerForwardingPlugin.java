package org.testcharm.cucumber;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
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
    }
}
