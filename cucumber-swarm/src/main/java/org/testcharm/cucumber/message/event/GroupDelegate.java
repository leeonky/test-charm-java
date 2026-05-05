package org.testcharm.cucumber.message.event;

import io.cucumber.plugin.event.Group;

import java.util.Collection;

public class GroupDelegate implements Group {
    private Collection<Group> children;
    private String value;
    private int start;
    private int end;

    @Override
    public Collection<Group> getChildren() {
        return children;
    }

    public void setChildren(Collection<Group> children) {
        this.children = children;
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
}
