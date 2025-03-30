package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.Element;

public class WorkspacePage extends Tab {
    private final Tabs<OutputPage, Element> outputs;

    public WorkspacePage(Element header, Element element) {
        super(header, element);
        outputs = new Tabs<OutputPage, Element>(element.byCss(".code-results")) {
        };
    }

    public OutputPage Current() {
        return outputs.getCurrent();
    }
}
