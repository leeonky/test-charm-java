package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Element;
import com.github.leeonky.dal.runtime.ProxyObject;
import com.github.leeonky.pf.Elements;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.leeonky.pf.By.css;

public class WatchesRegion extends OutputRegion implements ProxyObject {
    public WatchesRegion(Element header, Element tab) {
        super(header, tab);
    }

    public Elements<Element> watches() {
        return locate("css[.watches-item]");
    }

    @Override
    public WatchesItem getValue(Object property) {
        return watches().stream().filter(panel -> property.equals(panel.find(css(".watches-item-name")).single().text()))
                .map(WatchesItem::new)
                .findFirst().orElse(null);
    }

    @Override
    public Set<Object> getPropertyNames() {
        return watches().stream().map(panel -> (Object) panel.find(css(".watches-item-name")).single().text())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
