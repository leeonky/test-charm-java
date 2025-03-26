package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import com.github.leeonky.util.BeanClass;
import org.openqa.selenium.By;

import java.util.List;

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
        List<D> list = await("No elements found by: " + css).ignoreExceptions()
                .until(() -> findElements(By.cssSelector(css)), not(List::isEmpty));
        if (list.size() > 1)
            throw new IllegalStateException("Found more than one elements: " + css);
        return list.get(0);
    }
}
