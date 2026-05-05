package org.testcharm.cucumber.message.event;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStep;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class TestCaseDelegate implements TestCase {
    private List<TestStep> testSteps = new LinkedList<>();
    private UUID id;
    private Pickle pickle;

    @Override
    public Integer getLine() {
        return pickle.getLocation().getLine();
    }

    @Override
    public Location getLocation() {
        return pickle.getLocation();
    }

    @Override
    public String getKeyword() {
        return pickle.getKeyword();
    }

    @Override
    public String getName() {
        return pickle.getName();
    }

    @Override
    public String getScenarioDesignation() {
        return String.format("%s:%d#%s",
                pickle.getUri().getSchemeSpecificPart(), pickle.getLocation().getLine(), pickle.getName());
    }

    @Override
    public List<String> getTags() {
        return pickle.getTags();
    }

    @Override
    public List<TestStep> getTestSteps() {
        return testSteps;
    }

    public void setTestSteps(List<TestStep> testSteps) {
        this.testSteps = testSteps;
    }

    @Override
    public URI getUri() {
        return pickle.getUri();
    }

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setPickle(Pickle pickle) {
        this.pickle = pickle;
    }
}
