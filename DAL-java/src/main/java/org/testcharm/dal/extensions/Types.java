package org.testcharm.dal.extensions;

import org.testcharm.dal.DAL;
import org.testcharm.dal.runtime.*;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.dal.runtime.checker.Checker;
import org.testcharm.dal.runtime.checker.CheckerSet;
import org.testcharm.util.Sneaky;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Optional.of;
import static org.testcharm.dal.runtime.Order.BUILD_IN;

@Order(BUILD_IN)
public class Types implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder builder = dal.getRuntimeContextBuilder();
        builder.registerPropertyAccessor(Map.class, new MapPropertyAccessor())
                .registerPropertyAccessor(AutoMappingList.class, new AutoMappingListPropertyAccessor())
                .registerDALCollectionFactory(Iterable.class, IterableDALCollection::new)
                .registerDALCollectionFactory(Collection.class, CollectionDALCollection::new)
                .registerDALCollectionFactory(Stream.class, (stream) ->
                        new IterableDALCollection<Object>(stream::iterator))
                .registerDALCollectionFactory(DALCollection.class, instance -> instance)
                .registerDataRemark(DataRemarkParameterAcceptor.class, remarkData ->
                        remarkData.data().value().apply(remarkData.remark()))
                .registerPropertyAccessor(ProxyObject.class, new PropertyAccessor<ProxyObject>() {
                    @Override
                    public Object getValue(ProxyObject proxyObject, Object property) {
                        return proxyObject.getValue(property);
                    }

                    @Override
                    public Set<?> getPropertyNames(ProxyObject proxyObject) {
                        return proxyObject.getPropertyNames();
                    }

                    @Override
                    public boolean isNull(ProxyObject proxyObject) {
                        return proxyObject.isNull();
                    }
                })
                .registerReturnHook(d -> d.cast(Scoped.class).ifPresent(Scoped::onExit))
                .registerDALCollectionFactory(AdaptiveList.class, AdaptiveList::list)
                .registerPropertyAccessor(AdaptiveList.class, new PropertyAccessor<AdaptiveList<?>>() {

                    @Override
                    public Data<?> getData(Data<AdaptiveList<?>> data, Object property) {
                        return adaptiveListOf(data, d -> d.map(AdaptiveList::single).property(property), ExpressionException::illegalOp2);
                    }

                    @Override
                    public Set<?> getPropertyNames(Data<AdaptiveList<?>> data) {
                        return adaptiveListOf(data, d -> d.map(AdaptiveList::single).fieldNames(), ExpressionException::illegalOp2);
                    }
                })
                .registerMetaPropertyPattern(AdaptiveList.class, ".*",
                        (RuntimeDataHandler<MetaData<AdaptiveList>>) metaData -> {
                            if (metaData.name().equals("size") || metaData.name().equals("this"))
                                return metaData.delegate(d -> d.map(AdaptiveList::list));
                            else
                                return metaData.delegate(d -> adaptiveListOf(Sneaky.cast(d),
                                        l -> l.map(AdaptiveList::single), ExpressionException::illegalOp2));
                        })
        ;

        verifySingle(builder.checkerSetForEqualing());
        verifySingle(builder.checkerSetForMatching());
    }

    @SuppressWarnings("unchecked")
    private void verifySingle(CheckerSet checkerSet) {
        checkerSet.register((expected, actual) -> {
            if (actual.instanceOf(AdaptiveList.class)) {
                Data<Object> single = adaptiveListOf((Data<AdaptiveList<?>>) actual, l -> l.map(AdaptiveList::single),
                        ExpressionException::illegalOp1);
                Checker checkerOfElement = checkerSet.fetch(expected, single);
                return of(new Checker() {
                    @Override
                    public Data<?> verify(Data<?> expected1, Data<?> actual1, DALRuntimeContext context) {
                        return checkerOfElement.verify(expected1, single, context);
                    }
                });
            }
            return Optional.empty();
        });
    }

    private <T> T adaptiveListOf(Data<AdaptiveList<?>> data, Function<Data<AdaptiveList<?>>, T> function,
                                 Function<String, ExpressionException> exceptionSupplier) {
        try {
            return function.apply(data);
        } catch (InvalidAdaptiveListException e) {
            throw exceptionSupplier.apply(e.getMessage() + ", but is: " + data.map(ig -> e.list()).dump());
        }
    }
}
