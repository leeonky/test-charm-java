package com.github.leeonky.dal.extensions.inspector.cucumber.page;

public class InputField implements StringSetter {
    private final Panel panel;

    public InputField(Panel panel) {
        this.panel = panel;
    }

    @Override
    public void assign(String value) {
        panel.typeIn(value);
    }

    public String value() {
        return panel.attribute("value");
    }
}
