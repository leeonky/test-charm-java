package org.testcharm.dal.extensions.inspector.cucumber.page;

import org.testcharm.dal.extensions.inspector.cucumber.page.e.Element;
import org.testcharm.pf.AbstractPanel;
import org.testcharm.pf.Elements;

public class WatchesItem extends AbstractPanel<Element> {
    public WatchesItem(Element element) {
        super(element);
    }

    @Override
    public String text() {
        return perform("css[.watches-item-content].text");
    }

    public Elements<Element> image() {
        return locate("css[img]");
    }

    public Elements<Element> download() {
        return locate("css[a]");
    }
}
