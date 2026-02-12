package com.github.leeonky.extensions.dal;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.dal.runtime.checker.Checker;
import com.github.leeonky.dal.runtime.checker.CheckingContext;
import com.github.leeonky.jfactory.Collector;

import java.util.Optional;

import static com.github.leeonky.jfactory.Collector.Type.LIST;

public class ExtensionForBuild implements com.github.leeonky.dal.runtime.Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerOperator(Operators.EQUAL, new Operation<Collector, ExpectationFactory>() {
            @Override
            public boolean match(Data<?> v1, DALOperator operator, Data<?> v2, RuntimeContextBuilder.DALRuntimeContext context) {
                return v1.instanceOf(Collector.class) && v2.instanceOf(ExpectationFactory.class);
            }

            @Override
            public Data<?> operateData(Data<Collector> v1, DALOperator operator, Data<ExpectationFactory> v2,
                                       RuntimeContextBuilder.DALRuntimeContext context) {
                v1.value().raw();
                return v2.value().create(operator, v1).equalTo();
            }
        });

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
                        new InfiniteDALCollection<Collector>(() -> {
                            instance.forceType(LIST);
                            return null;
                        }) {
                            @Override
                            protected Collector getByPosition(int position) {
                                return instance.collect(position);
                            }
                        })
                .registerExclamation(Collector.class, runtimeData -> {
                    runtimeData.data().value().intently();
                    return runtimeData.data().value();
                })
        ;
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
