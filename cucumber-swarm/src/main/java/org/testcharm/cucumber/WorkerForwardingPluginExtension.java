package org.testcharm.cucumber;

public interface WorkerForwardingPluginExtension {
    void setEventPublisher(io.cucumber.plugin.event.EventPublisher eventPublisher);
}
