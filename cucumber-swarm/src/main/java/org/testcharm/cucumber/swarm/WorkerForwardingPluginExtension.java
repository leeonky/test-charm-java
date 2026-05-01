package org.testcharm.cucumber.swarm;

public interface WorkerForwardingPluginExtension {
    void setEventPublisher(io.cucumber.plugin.event.EventPublisher eventPublisher);
}
