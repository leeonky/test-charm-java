package com.github.leeonky.extensions.dal;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.checker.Checker;
import com.github.leeonky.dal.runtime.checker.CheckingContext;
import com.github.leeonky.pf.Element;
import com.github.leeonky.pf.WebElement;
import com.github.leeonky.util.BeanClass;

public class ElementExtension implements Extension {
    public static final Checker PHONY_CHECKER = new Checker() {
        @Override
        public String message(CheckingContext checkingContext) {
            return "Phony verification opt!!!";
        }

        @Override
        public boolean failed(CheckingContext checkingContext) {
            return false;
        }
    };

    @Override
    @SuppressWarnings("unchecked")
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .checkerSetForMatching().register((expected, actual) -> actual.cast(Element.class).map(e -> {
                    if (e.isInput()) {
                        e.typeIn(expected.convert(String.class).instance());
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
