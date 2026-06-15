package org.testcharm.pf;

import java.util.ArrayList;
import java.util.List;

public class PanelGroup<P extends Panel<? extends Element<?, ?, ?>>> {
    protected List<P> opened = new ArrayList<>();

    public List<P> opened() {
        return opened;
    }

    @SuppressWarnings("unchecked")
    public <T extends P> T open(Target<T> target) {
        return (T) opened.stream().filter(p -> target.matches((T) p)).findFirst().orElseGet(() -> {
            target.navigateTo();
            T e = target.create();
            opened.add(e);
            return e;
        });
    }
}
