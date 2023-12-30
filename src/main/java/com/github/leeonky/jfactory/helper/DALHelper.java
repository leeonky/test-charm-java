package com.github.leeonky.jfactory.helper;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.runtime.*;

import java.util.Optional;
import java.util.Set;

import static com.github.leeonky.jfactory.helper.ObjectReference.RawType.*;
import static java.util.Collections.emptySet;
import static java.util.Optional.of;

public class DALHelper {
    public DAL dal() {
        DAL dal = new DAL().extend();
        overrideOptEqual(dal);
        overrideOptMatch(dal);
        implementPropertyAssignment(dal);
        implementListElementAssignment(dal);
        implementCollectionSpec(dal);
        implementTraitSpec(dal);
        implementForceCreation(dal);

        dal.getRuntimeContextBuilder().registerDumper(ObjectReference.class, _ignore -> (data, dumpingBuffer) ->
                dumpingBuffer.dump(dumpingBuffer.getRuntimeContext()
                        .wrap(((ObjectReference) data.instance()).value())));
        return dal;
    }

    private void implementCollectionSpec(DAL dal) {
        dal.getRuntimeContextBuilder().registerPropertyAccessor(Specs.class, new PropertyAccessor<Specs>() {
            @Override
            public Object getValue(Specs specs, Object property) {
                return specs.addData((String) property).getData();
            }

            @Override
            public Set<Object> getPropertyNames(Specs instance) {
                return emptySet();
            }
        });
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
                        return reference.touchElement(position, super.getByPosition(position));
                    }
                });
    }

    private void implementPropertyAssignment(DAL dal) {
        dal.getRuntimeContextBuilder().registerPropertyAccessor(ObjectReference.class, new PropertyAccessor<ObjectReference>() {
            @Override
            public Object getValue(ObjectReference builder, Object property) {
                if (property.equals("_"))
                    return new LegacyTraitSetter(builder);
                return builder.add((String) property);
            }

            @Override
            public Set<Object> getPropertyNames(ObjectReference instance) {
                return emptySet();
            }
        });
    }

    private void overrideOptMatch(DAL dal) {
        dal.getRuntimeContextBuilder().registerOperator(Operators.MATCH, new Operation() {
            @Override
            public boolean match(Data v1, DALOperator operator, Data v2, RuntimeContextBuilder.DALRuntimeContext context) {
                return v1.instance() instanceof ObjectReference && v2.instance() instanceof ExpectationFactory;
            }

            @Override
            public Data operate(Data v1, DALOperator operator, Data v2, RuntimeContextBuilder.DALRuntimeContext context) {
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
                    if (actual.instance() instanceof LegacyTraitSetter)
                        return of(new OverrideVerificationOptChecker<>(LegacyTraitSetter::addTraitSpec));
                    return actual.instance() instanceof ObjectReference
                            ? of(new OverrideVerificationOptChecker<>(ObjectReference::setValue)) : Optional.empty();
                });
    }

    private void overrideOptEqual(DAL dal) {
        dal.getRuntimeContextBuilder().registerOperator(Operators.EQUAL, new Operation() {
            @Override
            public boolean match(Data v1, DALOperator operator, Data v2, RuntimeContextBuilder.DALRuntimeContext context) {
                return v1.instance() instanceof ObjectReference && v2.instance() instanceof ExpectationFactory;
            }

            @Override
            public Data operate(Data v1, DALOperator operator, Data v2, RuntimeContextBuilder.DALRuntimeContext context) {
                ExpectationFactory.Expectation expectation = ((ExpectationFactory) v2.instance()).create(operator, v1);
                ExpectationFactory.Type type = expectation.type();
                if (type == ExpectationFactory.Type.OBJECT)
                    ((ObjectReference) v1.instance()).rawType(RAW_OBJECT);
                else if (type == ExpectationFactory.Type.LIST)
                    ((ObjectReference) v1.instance()).rawType(RAW_LIST);
                return expectation.equalTo();
            }
        });
        dal.getRuntimeContextBuilder().checkerSetForEqualing()
                .register((expected, actual) -> actual.instance() instanceof ObjectReference
                        ? of(new OverrideVerificationOptChecker<>(ObjectReference::setValue)) : Optional.empty());
    }
}
