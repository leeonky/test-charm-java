package org.testcharm.dal.extensions.basic.collector;

import org.testcharm.dal.DAL;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.InfiniteDALCollection;
import org.testcharm.dal.runtime.PropertyAccessor;
import org.testcharm.dal.runtime.checker.Checker;
import org.testcharm.dal.runtime.checker.CheckingContext;
import org.testcharm.dal.runtime.inspector.DumperFactory;
import org.testcharm.util.Collector;

import java.util.Optional;

public class Extension implements org.testcharm.dal.runtime.Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().checkerSetForEqualing()
                .register((expected, actual) -> verificationOptAsAssignmentOpt(actual));
        dal.getRuntimeContextBuilder().checkerSetForMatching()
                .register((expected, actual) -> verificationOptAsAssignmentOpt(actual));

        dal.getRuntimeContextBuilder()
                .registerPropertyAccessor(Collector.class, new PropertyAccessor<Collector>() {
                    @Override
                    public Object getValue(Collector collector, Object property) {
                        return collector.collect(property);
                    }
                })

                .registerDALCollectionFactory(Collector.class, instance -> {
                    instance.type(Collector.Type.LIST);
//                List mapping need foreach auto collect
//                List mapping *** (skip element) should not auto collect
//                getByIndex should not auto collect
                    return new InfiniteDALCollection<Collector>(Collector::new) {
                        @Override
                        protected Collector getByPosition(int position) {
                            return instance.collect(position);
                        }
                    };
                })
                .registerDumper(Collector.class, DumperFactory.skip());
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
