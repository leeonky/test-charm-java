package org.testcharm.cucumber.message.event;

import io.cucumber.plugin.event.Argument;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Step;
import io.cucumber.plugin.event.StepArgument;

import java.net.URI;
import java.util.List;

public class PickleStepTestStepDelegate extends TestStepDelegate implements PickleStepTestStep {
    private String pattern;
    private Step step;
    private List<Argument> definitionArgument;
    private StepArgument stepArgument;
    private int stepLine;
    private URI uri;
    private String stepText;

    @Override
    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    @Override
    public List<Argument> getDefinitionArgument() {
        return definitionArgument;
    }

    public void setDefinitionArgument(List<Argument> definitionArgument) {
        this.definitionArgument = definitionArgument;
    }

    @Override
    public StepArgument getStepArgument() {
        return step.getArgument();
    }

    @Override
    public int getStepLine() {
        return step.getLine();
    }

    @Override
    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    @Override
    public String getStepText() {
        return step.getText();
    }
}
