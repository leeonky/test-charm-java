package com.github.leeonky.extensions.dal;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.inspector.cucumber.page.SingleSetter;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.checker.Checker;
import com.github.leeonky.dal.runtime.checker.CheckingContext;

import static com.github.leeonky.util.BeanClass.cast;

public class AssignableExtension implements Extension {

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
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .checkerSetForMatching().register((expected, actual) ->
                        cast(actual.instance(), SingleSetter.class).map(setter -> {
                            setter.assign(expected.convert(setter.getType()).instance());
                            return PHONY_CHECKER;
                        }))
        ;
        dal.getRuntimeContextBuilder()
                .checkerSetForEqualing().register((expected, actual) ->
                        cast(actual.instance(), SingleSetter.class).map(setter -> {
                            setter.assign(expected.instance());
                            return PHONY_CHECKER;
                        }));
    }
}
