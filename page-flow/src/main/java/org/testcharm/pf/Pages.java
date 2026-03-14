package org.testcharm.pf;

public class Pages<P> {
    protected P current = null;

    public P getCurrent() {
        return current;
    }

    @SuppressWarnings("unchecked")
    public <T extends P> T switchTo(Target<P> target) {
        current = getCurrent();
        if (current != null && target.matches(current))
            return (T) current;
        target.navigateTo();
        return (T) (current = target.create());
    }
}
