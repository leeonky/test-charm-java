package com.github.leeonky.extensions.dal;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Element;
import com.github.leeonky.dal.runtime.Extension;

import static com.github.leeonky.extensions.dal.AssignableExtension.PHONY_CHECKER;

public class ElementExtension implements Extension {
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .checkerSetForMatching().register((expected, actual) -> {
                    return actual.resolved().cast(Element.class).map(e -> {
                        if (e.isInput()) {
                            e.fillIn((String) expected.convert(String.class).instance());
                            return PHONY_CHECKER;
                        }
                        return null;
                    });
                })
        ;
    }
}
