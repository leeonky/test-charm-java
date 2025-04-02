package com.github.leeonky.dal.extensions.inspector.cucumber.pagebk;

import com.github.leeonky.dal.runtime.ProxyObject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WatchesPageBk implements ProxyObject {
    private final Panel panel;

    public WatchesPageBk(Panel panel) {
        this.panel = panel;
    }

    public List<Panel> watches() {
        return panel.allByCss(".watches-item");
    }

    @Override
    public WatchesItemBk getValue(Object property) {
        return watches().stream().filter(panel -> property.equals(panel.byCss(".watches-item-name").text()))
                .map(WatchesItemBk::new)
                .findFirst().orElse(null);
    }

    @Override
    public Set<Object> getPropertyNames() {
        return watches().stream().map(panel -> (Object) panel.byCss(".watches-item-name").text())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
