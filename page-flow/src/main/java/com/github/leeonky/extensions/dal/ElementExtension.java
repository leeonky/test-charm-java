package com.github.leeonky.extensions.dal;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.checker.Checker;
import com.github.leeonky.dal.runtime.checker.CheckingContext;
import com.github.leeonky.pf.Element;
import com.github.leeonky.pf.Elements;
import com.github.leeonky.pf.WebElement;
import com.github.leeonky.util.BeanClass;

public class ElementExtension implements Extension {
    public static final Checker PHONY_CHECKER = new Checker() {
        @Override
        public boolean failed(CheckingContext checkingContext) {
            return false;
        }
    };

    @Override
    @SuppressWarnings("unchecked")
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .checkerSetForMatching()
                .register((expected, actual) -> actual.cast(Element.class)
                        .map(e -> inputToElement(expected, e)))
                .register((expected, actual) -> actual.cast(Elements.class)
                        .map(e -> inputToElement(expected, (Element<?, ?>) e.single())));

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

    private static Checker inputToElement(Data<?> expected, Element<?, ?> e) {
        if (e.isInput()) {
            e.fillIn(expected.convert(String.class).value());
            return PHONY_CHECKER;
        }
        return null;
    }
}
