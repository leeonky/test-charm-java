package org.testcharm.cucumber.message.event;

import io.cucumber.plugin.event.Argument;
import io.cucumber.plugin.event.Group;

public class ArgumentDelegate implements Argument {
    private String parameterTypeName;
    private String value;
    private int start;
    private int end;
    private Group group;

    @Override
    public String getParameterTypeName() {
        return parameterTypeName;
    }

    public void setParameterTypeName(String parameterTypeName) {
        this.parameterTypeName = parameterTypeName;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    @Override
    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
