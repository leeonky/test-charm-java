package com.github.leeonky.dal.extensions.inspector.cucumber.ui;

public interface WebElement<T extends SeleniumElement<T, E>, E> extends Element<T, E> {

    @Override
    default boolean isInput() {
        String tag = getTag().toLowerCase();
        return tag.equals("textarea") || tag.equals("input");
    }
}
