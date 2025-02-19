package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ViewFrame {
    protected final Browser browser;
    protected Object current = null;

    public ViewFrame(Browser browser) {
        this.browser = browser;
    }

    public <T> T switchTo(Runnable open, Supplier<T> constructor, Class<T> frameType) {
        return switchTo(open, constructor, frameType::isInstance);
    }

    @SuppressWarnings("unchecked")
    public <T> T switchTo(Runnable open, Supplier<T> constructor, Predicate<Object> condition) {
        if (condition.test(current))
            return (T) current;
        open.run();
        return (T) (current = constructor.get());
    }
}
