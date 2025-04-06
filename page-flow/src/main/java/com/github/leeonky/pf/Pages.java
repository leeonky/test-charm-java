package com.github.leeonky.pf;

public class Pages<P> {
    protected P current = null;

    public P getCurrent() {
        return current;
    }

    @SuppressWarnings("unchecked")
    public <T extends P> T switchTo(Target<T> target) {
        current = getCurrent();
        if (current != null && target.matches((T) current))
            return (T) current;
        target.navigateTo();
        return (T) (current = target.create());
    }
}
