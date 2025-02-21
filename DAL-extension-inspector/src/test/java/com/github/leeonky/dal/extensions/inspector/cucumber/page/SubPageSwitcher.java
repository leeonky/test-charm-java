package com.github.leeonky.dal.extensions.inspector.cucumber.page;

public class SubPageSwitcher<P> {
    protected P current = null;

    @SuppressWarnings("unchecked")
    public <T extends P> T switchTo(Target<T> target) {
        if (current != null && target.matches((T) current))
            return (T) current;
        target.navigateTo();
        return (T) (current = target.create());
    }
}
