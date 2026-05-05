package org.testcharm.cucumber.message.event;

import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.HookType;

public class HookTestStepDelegate extends TestStepDelegate implements HookTestStep {
    private HookType hookType;

    @Override
    public HookType getHookType() {
        return hookType;
    }

    public void setHookType(HookType hookType) {
        this.hookType = hookType;
    }
}
