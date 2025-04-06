package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Element;
import com.github.leeonky.dal.runtime.ProxyObject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.leeonky.pf.By.css;

public class WatchesRegion extends OutputRegion implements ProxyObject {
    public WatchesRegion(Element header, Element tab) {
        super(header, tab);
    }

    public List<Element> watches() {
        return element.findAllBy(css(".watches-item"));
    }

    @Override
    public WatchesItem getValue(Object property) {
        return watches().stream().filter(panel -> property.equals(panel.findBy(css(".watches-item-name")).text()))
                .map(WatchesItem::new)
                .findFirst().orElse(null);
    }

    @Override
    public Set<Object> getPropertyNames() {
        return watches().stream().map(panel -> (Object) panel.findBy(css(".watches-item-name")).text())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
