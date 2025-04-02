package com.github.leeonky.dal.extensions.inspector.cucumber.ui;

public interface WebElement<T extends WebElement<T, E>, E> extends Element<T, E> {
    @Override
    default boolean isInput() {
        String tag = getTag().toLowerCase();
        return tag.equals("textarea") || tag.equals("input");
    }

    default Object attribute(String name) {
        String value = attributeValue(name);
        return name.equals("class") ? value.split(" ") : value;
    }

    String attributeValue(String name);

    @Override
    default Object value() {
        if (isInput())
            return attribute("value");
        return Element.super.value();
    }
}
