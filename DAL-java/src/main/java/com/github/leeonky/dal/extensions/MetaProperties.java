package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.checker.Checker;
import com.github.leeonky.dal.runtime.checker.CheckingContext;
import com.github.leeonky.interpreter.SyntaxException;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.NoSuchAccessorException;
import com.github.leeonky.util.Sneaky;

import java.util.Set;
import java.util.stream.Collectors;

import static com.github.leeonky.dal.runtime.DALException.extractException;
import static com.github.leeonky.dal.runtime.Operators.MATCH;
import static com.github.leeonky.dal.runtime.Order.BUILD_IN;
import static java.util.Arrays.asList;

@Order(BUILD_IN)
public class MetaProperties implements Extension {
    private static Object size(MetaData<?> metaData) {
        return metaData.data().list().size();
    }

    private static Object throw_(MetaData<?> metaData) {
        try {
            metaData.data().value();
            throw new AssertionError("Expecting an error to be thrown, but nothing was thrown");
        } catch (Exception e) {
            return Sneaky.get(() -> extractException(e).orElseThrow(() -> e));
        }
    }

    private static Object object_(MetaData<?> metaData) {
        return metaData.data().value() == null ? null : new OriginalJavaObject(metaData.data());
    }

    private static Object keys(MetaData<?> metaData) {
        return metaData.data().fieldNames();
    }

    private static Object entries(MetaData<?> metaData) {
        return metaData.data().fieldNames().stream().map(k ->
                asList(k, metaData.data().property(k).value())).collect(Collectors.toList());
    }

    private static Object flat(MetaData<?> metaData) {
        Object[] array = metaData.data().list().wraps().stream()
                .flatMap(dataIndexedElement -> dataIndexedElement.value().list().values())
                .toArray();
        return array
                ;
    }

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerMetaProperty("size", MetaProperties::size)
                .registerMetaProperty("flat", MetaProperties::flat)
                .registerMetaProperty("throw", MetaProperties::throw_)
                .registerMetaProperty("object", MetaProperties::object_)
                .registerMetaProperty("keys", MetaProperties::keys)
                .registerMetaProperty("entries", MetaProperties::entries)
                .registerMetaProperty("should", MetaShould::new)
                .registerMetaProperty("this", (RuntimeDataHandler<MetaData<?>>) RuntimeData::data)
                .registerMetaProperty(MetaShould.class, "not", (metaData) -> metaData.data().value().negative())
                .registerMetaProperty("root", (RuntimeDataHandler<MetaData<?>>) metaData ->
                        metaData.runtimeContext().inputRoot())
        ;

        dal.getRuntimeContextBuilder().checkerSetForMatching()
                .register((expected, actual) -> actual.cast(MetaShould.PredicateMethod.class).map(predicateMethod -> new Checker() {
                    private MetaShould.PredicateMethod resolved;

                    @Override
                    public boolean failed(CheckingContext checkingContext) {
                        return !(resolved = (MetaShould.PredicateMethod) predicateMethod.getValue(expected.value())).should();
                    }

                    @Override
                    public String message(CheckingContext checkingContext) {
                        return resolved.errorMessage();
                    }
                }))
                .register((expected, actual) -> actual.cast(CurryingMethod.class).map(curryingMethod -> new Checker() {
                    @Override
                    public Data<?> verify(Data<?> expected, Data<?> actual, DALRuntimeContext context) {
                        return actual.property(expected.value());
                    }
                }))
        ;

        dal.getRuntimeContextBuilder().checkerSetForEqualing()
                .register((expected, actual) -> actual.cast(MetaShould.PredicateMethod.class).map(predicateMethod -> new Checker() {
                    @Override
                    public boolean failed(CheckingContext checkingContext) {
                        throw ExpressionException.exception(expression -> new SyntaxException("Should use `:` in ::should verification",
                                expression.operator().getPosition()));
                    }
                }));

        dal.getRuntimeContextBuilder().registerOperator(MATCH, new Operation<CurryingMethod, ExpectationFactory>() {

            @Override
            public boolean match(Data<?> v1, DALOperator operator, Data<?> v2, DALRuntimeContext context) {
                return v1.instanceOf(CurryingMethod.class) && v2.instanceOf(ExpectationFactory.class);
            }

            @Override
            public Data<?> operateData(Data<CurryingMethod> v1, DALOperator operator, Data<ExpectationFactory> v2, DALRuntimeContext context) {
                return v2.value().create(operator, v1).matches();
            }
        });
    }

    static class OriginalJavaObject implements ProxyObject {
        private final Data<?> data;

        public OriginalJavaObject(Data<?> data) {
            this.data = data;
        }

        @Override
        public Object getValue(Object property) {
            try {
                Object instance = data.value();
                return BeanClass.createFrom(instance).getPropertyValue(instance, property.toString());
            } catch (NoSuchAccessorException ignore) {
                return data.property(property).value();
            }
        }

        @Override
        public Set<?> getPropertyNames() {
            return BeanClass.createFrom(data.value()).getPropertyReaders().keySet();
        }
    }
}
