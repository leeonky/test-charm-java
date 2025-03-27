package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.util.BeanClass;

import java.util.List;

import static com.github.leeonky.dal.extensions.inspector.cucumber.page.By.css;
import static com.github.leeonky.util.function.Extension.not;
import static org.awaitility.Awaitility.await;

public interface Element<D extends Element<D, E>, E> {

    @SuppressWarnings("unchecked")
    default D newInstance(E element) {
        return (D) BeanClass.create(getClass()).newInstance(element);
    }

    String getText();

    List<D> findElements(By by);

    default D byCss(String css) {
        return by(css(css));
    }

    default D by(By by) {
        List<D> list = await("No elements found by: " + by).ignoreExceptions()
                .until(() -> findElements(by), not(List::isEmpty));
        if (list.size() > 1)
            throw new IllegalStateException("Found more than one elements: " + by);
        return list.get(0);
    }
}
