package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.InspectorElement;

public class MainPage {
    private final InspectorElement region;

    public MainPage(InspectorElement region) {
        this.region = region;
    }

    public InspectorElement title() {
        return region.byCss(".main-title");
    }
}
