package org.testcharm.dal.extensions.inspector.cucumber.page;

import org.testcharm.dal.extensions.inspector.cucumber.page.e.Element;
import org.testcharm.dal.extensions.inspector.cucumber.page.e.Tab;

public class OutputRegion extends Tab {
    public OutputRegion(Element header, Element element) {
        super(header, element);
    }

    @Override
    public String toString() {
        return element().text();
    }
}
