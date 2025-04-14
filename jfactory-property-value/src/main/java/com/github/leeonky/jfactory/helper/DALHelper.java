package com.github.leeonky.jfactory.helper;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.runtime.*;

import java.util.Optional;

import static com.github.leeonky.jfactory.helper.ObjectReference.RawType.*;
import static java.util.Optional.of;

public class DALHelper {
    public DAL dal() {
        DAL dal = new DAL("JFactory").extend();
        overrideOptEqual(dal);
        overrideOptMatch(dal);
        implementListElementAssignment(dal);
        implementTraitSpec(dal);
        implementForceCreation(dal);

        dal.getRuntimeContextBuilder().registerDumper(ObjectReference.class, _ignore -> (data, dumpingBuffer) ->
                dumpingBuffer.dump(data.getValue("value")));
        return dal;
    }

    private void implementForceCreation(DAL dal) {
        dal.getRuntimeContextBuilder().registerExclamation(ObjectReference.class, runtimeData -> {
            ((ObjectReference) runtimeData.data().instance()).intently();
            return runtimeData.data();
        });
    }

    private void implementTraitSpec(DAL dal) {
        dal.getRuntimeContextBuilder().registerDataRemark(ObjectReference.class, remarkData -> {
            ((ObjectReference) remarkData.data().instance()).addTraitSpec(remarkData.remark());
            return remarkData.data();
        });
    }

    private void implementListElementAssignment(DAL dal) {
        dal.getRuntimeContextBuilder().registerDALCollectionFactory(ObjectReference.class, reference ->
                new InfiniteDALCollection<ObjectReference>(ObjectReference::new) {
                    @Override
                    protected ObjectReference getByPosition(int position) {
                        return reference.getElement(position);
                    }
                });
    }

    private void overrideOptMatch(DAL dal) {
        dal.getRuntimeContextBuilder().registerOperator(Operators.MATCH, new Operation() {
            @Override
            public boolean match(Data<?> v1, DALOperator operator, Data<?> v2, RuntimeContextBuilder.DALRuntimeContext context) {
                return v1.instanceOf(ObjectReference.class) && v2.instanceOf(ExpectationFactory.class);
            }

            @Override
            public Data<?> operateData(Data<?> v1, DALOperator operator, Data<?> v2, RuntimeContextBuilder.DALRuntimeContext context) {
                ExpectationFactory.Expectation expectation = ((ExpectationFactory) v2.instance()).create(operator, v1);
                ExpectationFactory.Type type = expectation.type();
                if (type == ExpectationFactory.Type.OBJECT)
                    ((ObjectReference) v1.instance()).rawType(OBJECT);
                else if (type == ExpectationFactory.Type.LIST)
                    ((ObjectReference) v1.instance()).rawType(LIST);
                return expectation.matches();
            }
        });
        dal.getRuntimeContextBuilder().checkerSetForMatching()
                .register((expected, actual) -> {
                    if (actual.instanceOf(LegacyTraitSetter.class))
                        return of(new OverrideVerificationOptChecker<>(LegacyTraitSetter::addTraitSpec));
                    return actual.instanceOf(ObjectReference.class)
                            ? of(new OverrideVerificationOptChecker<>(ObjectReference::setValue)) : Optional.empty();
                });
    }

    private void overrideOptEqual(DAL dal) {
        dal.getRuntimeContextBuilder().registerOperator(Operators.EQUAL, new Operation() {
            @Override
            public boolean match(Data<?> v1, DALOperator operator, Data<?> v2, RuntimeContextBuilder.DALRuntimeContext context) {
                return v1.instanceOf(ObjectReference.class) && v2.instanceOf(ExpectationFactory.class);
            }

            @Override
            public Data<?> operateData(Data<?> v1, DALOperator operator, Data<?> v2, RuntimeContextBuilder.DALRuntimeContext context) {
                ExpectationFactory.Expectation expectation = ((ExpectationFactory) v2.instance()).create(operator, v1);
                ExpectationFactory.Type type = expectation.type();
                ObjectReference objectReference = (ObjectReference) v1.instance();
                if (type == ExpectationFactory.Type.OBJECT)
                    objectReference.rawType(RAW_OBJECT);
                else if (type == ExpectationFactory.Type.LIST)
                    objectReference.rawType(RAW_LIST);
                objectReference.clear();
                return expectation.equalTo();
            }
        });
        dal.getRuntimeContextBuilder().checkerSetForEqualing()
                .register((expected, actual) -> actual.instanceOf(ObjectReference.class)
                        ? of(new OverrideVerificationOptChecker<>(ObjectReference::setValue)) : Optional.empty());
    }
}
