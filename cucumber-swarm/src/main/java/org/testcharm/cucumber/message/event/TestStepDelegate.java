package org.testcharm.cucumber.message.event;

import io.cucumber.plugin.event.TestStep;

import java.util.UUID;

abstract class TestStepDelegate implements TestStep {
    private String codeLocation;
    private UUID id;

    @Override
    public String getCodeLocation() {
        return codeLocation;
    }

    public void setCodeLocation(String codeLocation) {
        this.codeLocation = codeLocation;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
