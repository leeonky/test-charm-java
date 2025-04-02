package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.Element;
import com.github.leeonky.dal.runtime.ProxyObject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.leeonky.dal.extensions.inspector.cucumber.ui.By.css;

public class WatchesPage extends OutputPage implements ProxyObject {
    public WatchesPage(Element header, Element tab) {
        super(header, tab);
    }

    public List<Element> watches() {
        return region.findAllBy(css(".watches-item"));
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
