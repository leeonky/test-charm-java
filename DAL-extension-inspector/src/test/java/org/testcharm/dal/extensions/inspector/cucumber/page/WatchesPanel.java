package org.testcharm.dal.extensions.inspector.cucumber.page;

import org.testcharm.dal.extensions.inspector.cucumber.page.e.Element;
import org.testcharm.dal.runtime.ProxyObject;
import org.testcharm.pf.Elements;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testcharm.pf.By.css;

public class WatchesPanel extends OutputPanel implements ProxyObject {
    public WatchesPanel(Element header, Element tab) {
        super(header, tab);
    }

    public Elements<Element> watches() {
        return locate("css[.watches-item]");
    }

    @Override
    public WatchesItem getValue(Object property) {
        return watches().list().values().filter(panel -> property.equals(panel.find(css(".watches-item-name")).single().text()))
                .map(WatchesItem::new)
                .findFirst().orElse(null);
    }

    @Override
    public Set<Object> getPropertyNames() {
        return watches().list().values().map(panel -> (Object) panel.find(css(".watches-item-name")).single().text())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
