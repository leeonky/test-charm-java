package com.github.leeonky.extensions.dal;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.InfiniteDALCollection;
import com.github.leeonky.dal.runtime.PropertyAccessor;
import com.github.leeonky.dal.runtime.checker.Checker;
import com.github.leeonky.dal.runtime.checker.CheckingContext;
import com.github.leeonky.jfactory.Collector;

import java.util.Optional;

public class ExtensionForBuild implements com.github.leeonky.dal.runtime.Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().checkerSetForEqualing()
                .register((expected, actual) -> verificationOptAsAssignmentOpt(actual));
        dal.getRuntimeContextBuilder().checkerSetForMatching()
                .register((expected, actual) -> verificationOptAsAssignmentOpt(actual));

        dal.getRuntimeContextBuilder()
                .registerDataRemark(Collector.class, remarkData ->
                        remarkData.data().value().setTraitsSpec(remarkData.remark().split(", |,| ")))
                .registerPropertyAccessor(Collector.class, new PropertyAccessor<Collector>() {
                    @Override
                    public Object getValue(Collector collector, Object property) {
                        return collector.collect(property);
                    }
                })
                .registerDALCollectionFactory(Collector.class, instance ->
                        new InfiniteDALCollection<Collector>(() -> null) {
                            @Override
                            protected Collector getByPosition(int position) {
                                return instance.collect(position);
                            }
                        });
    }

    private Optional<Checker> verificationOptAsAssignmentOpt(Data<?> actual) {
        if (actual.instanceOf(Collector.class))
            return Optional.of(new Checker() {
                @Override
                public boolean failed(CheckingContext checkingContext) {
                    ((Collector) checkingContext.getOriginalActual().value())
                            .setValue(checkingContext.getOriginalExpected().value());
                    return false;
                }
            });
        return Optional.empty();
    }
}
