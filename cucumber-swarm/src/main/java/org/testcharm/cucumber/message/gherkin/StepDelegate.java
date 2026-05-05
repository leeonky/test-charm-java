package org.testcharm.cucumber.message.gherkin;

import io.cucumber.core.gherkin.Argument;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.gherkin.StepType;
import io.cucumber.plugin.event.Location;

public class StepDelegate implements Step {
    private StepType type;
    private String previousGivenWhenThenKeyword;
    private String id;
    private Argument argument;
    private String keyword;
    private String text;
    private int line;
    private Location location;

    @Override
    public StepType getType() {
        return type;
    }

    public void setType(StepType type) {
        this.type = type;
    }

    @Override
    public String getPreviousGivenWhenThenKeyword() {
        return previousGivenWhenThenKeyword;
    }

    public void setPreviousGivenWhenThenKeyword(String previousGivenWhenThenKeyword) {
        this.previousGivenWhenThenKeyword = previousGivenWhenThenKeyword;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Argument getArgument() {
        return argument;
    }

    public void setArgument(Argument argument) {
        this.argument = argument;
    }

    @Override
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
