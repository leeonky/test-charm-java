package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Element;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Page;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Pages;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Target;
import com.github.leeonky.util.BeanClass;

import static com.github.leeonky.dal.extensions.inspector.cucumber.ui.By.css;
import static com.github.leeonky.dal.extensions.inspector.cucumber.ui.By.xpath;
import static java.lang.String.format;

public class Tabs<T extends Tab, E extends Element<E, ?>> extends Page<E> {
    final Pages<T> tabs;

    public Tabs(E element) {
        super(element);
        tabs = new Pages<T>() {
            @Override
            public T getCurrent() {
                return createTab(Tabs.this.region.findBy(xpath("./div[" + containsClass("tab-headers") + "]/div[contains(@class, 'tab-header')" + " and " + containsClass("active") + "]")),
                        Tabs.this.region.findBy(xpath("./div[" + containsClass("tab-contents") + "]/div[contains(@class, 'tab-content')" + " and " + containsClass("active") + "]"))
                );
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

    public T getCurrent() {
        return tabs.getCurrent();
    }

    public T switchTo(String name) {
        return tabs.switchTo(new Target<T>() {
            @Override
            public T create() {
                return createTab(region.findBy(css(format(".tab-header[target='%s']", name))),
                        region.findBy(css(format(".tab-content[target='%s']", name))));
            }

            @Override
            public void navigateTo() {
                region.findBy(css(format(".tab-header[target='%s']", name))).click();
            }
        });
    }
}