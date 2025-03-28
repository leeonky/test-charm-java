package com.github.leeonky.extensions.dal;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Element;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.WebElement;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.util.BeanClass;

import static com.github.leeonky.extensions.dal.AssignableExtension.PHONY_CHECKER;

public class ElementExtension implements Extension {
    @Override
    @SuppressWarnings("unchecked")
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .checkerSetForMatching().register((expected, actual) -> actual.resolved().cast(Element.class).map(e -> {
                    if (e.isInput()) {
                        e.fillIn((String) expected.convert(String.class).instance());
                        return PHONY_CHECKER;
                    }
                    return null;
                }));

        dal.getRuntimeContextBuilder().registerPropertyAccessor(WebElement.class,
                new JavaClassPropertyAccessor<WebElement<?, ?>>((BeanClass) BeanClass.create(WebElement.class)) {
                    @Override
                    public Object getValue(WebElement<?, ?> webElement, Object property) {
                        if (property instanceof String && ((String) property).startsWith("@"))
                            return webElement.attribute(((String) property).substring(1));
                        return super.getValue(webElement, property);
                    }
                });
    }
}
