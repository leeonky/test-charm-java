package com.github.leeonky.dal.extensions.inspector.cucumber.ui;

import com.github.leeonky.util.BeanClass;

import java.util.List;

import static com.github.leeonky.dal.extensions.inspector.cucumber.ui.By.css;
import static com.github.leeonky.dal.extensions.inspector.cucumber.ui.By.xpath;
import static com.github.leeonky.util.function.Extension.not;
import static org.awaitility.Awaitility.await;

public interface Element<T extends Element<T, E>, E> {

    @SuppressWarnings("unchecked")
    default T newInstance(E element) {
        return (T) BeanClass.create(getClass()).newInstance(element);
    }

    List<T> findElements(By by);

    default T byCss(String css) {
        return by(css(css));
    }

    default T byXpath(String xpath) {
        return by(xpath(xpath));
    }

    default T byText(String text) {
        return by(xpath(String.format(".//*[normalize-space(@value)='%s' or normalize-space(text())='%s']", text, text)));
    }

    default T by(By by) {
        List<T> list = await("No elements found by: " + by).ignoreExceptions()
                .until(() -> findElements(by), not(List::isEmpty));
        if (list.size() > 1)
            throw new IllegalStateException("Found more than one elements: " + by);
        return list.get(0);
    }

    String getText();

    void click();
}
