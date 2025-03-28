package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Element;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Page;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Pages;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Target;
import com.github.leeonky.util.BeanClass;

import static java.lang.String.format;

public class Tabs<T extends Tab, E extends Element<E, ?>> extends Page<E> {
    final Pages<T> tabs;

    public Tabs(E element) {
        super(element);
        tabs = new Pages<T>() {
            @Override
            public T getCurrent() {
                return createTab(Tabs.this.element.byXpath(".//div[" + containsClass("tab-header") + " and " + containsClass("active") + " and not(ancestor::div[" + containsClass("tab-content") + "])]"),
                        Tabs.this.element.byXpath(".//div[" + containsClass("tab-content") + " and  " + containsClass("active") + " and not(ancestor::div[" + containsClass("tab-content") + "])]"));
            }
        };
    }

    @SuppressWarnings("unchecked")
    protected T createTab(E header, E tab) {
        return (T) BeanClass.create(getClass()).getSuper(Tabs.class).getTypeArguments(0)
                .orElseThrow(() -> new IllegalStateException("Can not resolve generic type of: " + getClass()))
                .newInstance(header, tab);
    }

    private String containsClass(String singleClassName) {
        return "contains(concat(' ', normalize-space(@class), ' '), ' " + singleClassName + " ')";
    }

    T getCurrent() {
        return tabs.getCurrent();
    }

    T switchTo(String name) {
        return tabs.switchTo(new Target<T>() {
            @Override
            public T create() {
                return createTab(element.byCss(format(".tab-header[target='%s']", name)),
                        element.byCss(format(".tab-content[target='%s']", name)));
            }

            @Override
            public void navigateTo() {
                element.byCss(".work-bench-headers").byText(name).click();
            }
        });
    }
}