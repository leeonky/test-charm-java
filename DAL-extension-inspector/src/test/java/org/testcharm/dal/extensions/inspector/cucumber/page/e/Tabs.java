package org.testcharm.dal.extensions.inspector.cucumber.page.e;

import org.testcharm.pf.AbstractPanel;
import org.testcharm.pf.Element;
import org.testcharm.pf.PanelStack;
import org.testcharm.pf.Target;
import org.testcharm.util.BeanClass;

import static java.lang.String.format;
import static org.testcharm.pf.By.css;
import static org.testcharm.pf.By.xpath;

public class Tabs<T extends Tab, E extends Element<E, ?, ?>> extends AbstractPanel<E> {
    final PanelStack<T> tabs;

    public Tabs(E element) {
        super(element);
        tabs = new PanelStack<T>() {
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
            return createTab(element().patience("2s").find(xpath("./div[" + containsClass("tab-headers") + "]/div[contains(@class, 'tab-header')" + " and " + containsClass("active") + "]")).single(),
                    element().find(xpath("./div[" + containsClass("tab-contents") + "]/div[contains(@class, 'tab-content')" + " and " + containsClass("active") + "]")).single());
        } catch (Exception ignore) {
            return null;
        }
    }

    public T switchTo(String name) {
        return tabs.switchTo(new Target<T>() {
            @Override
            public T create() {
                return createTab(element().find(css(format(".tab-header[target='%s']", name))).single(),
                        element().find(css(format(".tab-content[target='%s']", name))).single());
            }

            @Override
            public void navigateTo() {
                perform(String.format("css['.tab-header[target=\"%s\"]'].click", name));
            }
        });
    }
}