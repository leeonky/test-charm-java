package org.testcharm.pf;

import org.testcharm.dal.extensions.basic.TimeUtil;

public abstract class AbstractElement<T extends Element<T, E>, E> implements Element<T, E> {
    private By locator;
    private T parent;
    private int timeout = -1;

    @Override
    public By getLocator() {
        return locator;
    }

    @Override
    public void setLocator(By locator) {
        this.locator = locator;
    }

    @Override
    public T patience(String time) {
        timeout = TimeUtil.parseTime(time);
        return (T) this;
    }

    @Override
    public int timeout() {
        if (timeout == -1)
            return defaultTimeout();
        return timeout;
    }

    @Override
    public T parent() {
        return parent;
    }

    @Override
    public void parent(T parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return getDom();
    }
}
