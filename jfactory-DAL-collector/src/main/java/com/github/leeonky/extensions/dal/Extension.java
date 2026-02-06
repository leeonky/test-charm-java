package com.github.leeonky.extensions.dal;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RemarkData;
import com.github.leeonky.dal.runtime.RuntimeDataHandler;
import com.github.leeonky.dal.runtime.checker.Checker;
import com.github.leeonky.dal.runtime.checker.CheckingContext;
import com.github.leeonky.jfactory.collector.UnitCollector;

import java.util.Optional;

public class Extension implements com.github.leeonky.dal.runtime.Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().checkerSetForEqualing()
                .register((expected, actual) -> verificationOptAsAssignmentOpt(actual));
        dal.getRuntimeContextBuilder().checkerSetForMatching()
                .register((expected, actual) -> verificationOptAsAssignmentOpt(actual));

        dal.getRuntimeContextBuilder().registerDataRemark(UnitCollector.class, new RuntimeDataHandler<RemarkData<UnitCollector>>() {
            @Override
            public Data<?> handleData(RemarkData<UnitCollector> remarkData) {
                return remarkData.data().map(unitCollector -> unitCollector.setTraitsSpec(remarkData.remark().split(", |,| ")));
            }
        });
    }

    private Optional<Checker> verificationOptAsAssignmentOpt(Data<?> actual) {
        if (actual.instanceOf(UnitCollector.class))
            return Optional.of(new Checker() {
                @Override
                public boolean failed(CheckingContext checkingContext) {
                    ((UnitCollector) checkingContext.getOriginalActual().value())
                            .setValue(checkingContext.getOriginalExpected().value());
                    return false;
                }
            });
        return Optional.empty();
    }
}
