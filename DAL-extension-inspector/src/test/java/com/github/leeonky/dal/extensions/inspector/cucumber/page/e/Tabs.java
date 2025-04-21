package com.github.leeonky.dal.extensions.inspector.cucumber.page.e;

import com.github.leeonky.pf.Element;
import com.github.leeonky.pf.Pages;
import com.github.leeonky.pf.Region;
import com.github.leeonky.pf.Target;
import com.github.leeonky.util.BeanClass;

import static com.github.leeonky.pf.By.css;
import static com.github.leeonky.pf.By.xpath;
import static java.lang.String.format;

public class Tabs<T extends Tab, E extends Element<E, ?>> extends Region<E> {
    final Pages<T> tabs;

    public Tabs(E element) {
        super(element);
        tabs = new Pages<T>() {
            @Override
            public T getCurrent() {
                return Tabs.this.getCurrent();
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
        try {
            return createTab(element.findAllBy(xpath("./div[" + containsClass("tab-headers") + "]/div[contains(@class, 'tab-header')" + " and " + containsClass("active") + "]")).get(0),
                    element.findAllBy(xpath("./div[" + containsClass("tab-contents") + "]/div[contains(@class, 'tab-content')" + " and " + containsClass("active") + "]")).get(0));
        } catch (Exception ignore) {
            return null;
        }
    }

    public T switchTo(String name) {
        return tabs.switchTo(new Target<T>() {
            @Override
            public T create() {
                return createTab(element.single(css(format(".tab-header[target='%s']", name))),
                        element.single(css(format(".tab-content[target='%s']", name))));
            }

            @Override
            public void navigateTo() {
                element.single(css(format(".tab-header[target='%s']", name))).click();
            }
        });
    }
}