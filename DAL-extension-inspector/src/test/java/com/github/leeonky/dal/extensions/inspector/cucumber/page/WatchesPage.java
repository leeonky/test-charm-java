package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.runtime.ProxyObject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WatchesPage implements ProxyObject {
    private final Panel panel;

    public WatchesPage(Panel panel) {
        this.panel = panel;
    }

    public List<Panel> watches() {
        return panel.allByCss(".watches-item");
    }

    @Override
    public WatchesItem getValue(Object property) {
        return watches().stream().filter(panel -> property.equals(panel.byCss(".watches-item-name").text()))
                .map(WatchesItem::new)
                .findFirst().orElse(null);
    }

    @Override
    public Set<Object> getPropertyNames() {
        return watches().stream().map(panel -> (Object) panel.byCss(".watches-item-name").text())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
