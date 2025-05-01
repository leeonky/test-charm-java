package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.checker.Checker;
import com.github.leeonky.dal.runtime.checker.CheckerSet;
import com.github.leeonky.util.Sneaky;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.leeonky.dal.runtime.ExpressionException.illegalOp2;
import static com.github.leeonky.dal.runtime.ExpressionException.opt1;
import static com.github.leeonky.dal.runtime.Order.BUILD_IN;
import static java.util.Optional.of;

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
                .registerReturnHook(d -> d.cast(ReturnHook.class).ifPresent(ReturnHook::onReturn))
                .registerDALCollectionFactory(AdaptiveList.class, AdaptiveList::list)
                .registerPropertyAccessor(AdaptiveList.class, new PropertyAccessor<AdaptiveList<?>>() {

                    @Override
                    public Data<?> getData(Data<AdaptiveList<?>> data, Object property, DALRuntimeContext context) {
                        return adaptiveListOf(data, d -> d.map(AdaptiveList::soloList).property(0).property(property));
                    }

                    @Override
                    public Set<?> getPropertyNames(Data<AdaptiveList<?>> data) {
                        return adaptiveListOf(data, d -> d.map(AdaptiveList::soloList).property(0).fieldNames());
                    }
                })
                .registerMetaPropertyPattern(AdaptiveList.class, ".*",
                        (RuntimeDataHandler<MetaData<AdaptiveList>>) metaData -> {
                            if (metaData.name().equals("size"))
                                return metaData.delegate(d -> d.map(AdaptiveList::list));
                            else
                                return metaData.delegate(d -> adaptiveListOf(Sneaky.cast(d),
                                        data -> data.map(AdaptiveList::soloList).property(0)));
                        })
        ;

        verifySingle(builder.checkerSetForEqualing());
        verifySingle(builder.checkerSetForMatching());
    }

    private void verifySingle(CheckerSet checkerSet) {
        checkerSet.register((expected, actual) -> {
            if (actual.instanceOf(AdaptiveList.class)) {
                Data<Object> single = opt1(() -> actual.map(t -> ((AdaptiveList<?>) t).single()));
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

    private <T> T adaptiveListOf(Data<AdaptiveList<?>> data, Function<Data<AdaptiveList<?>>, T> function) {
        try {
            return function.apply(data);
        } catch (InvalidAdaptiveListException e) {
            throw illegalOp2("Expected list can only have one element, but is: " + data.dump());
        }
    }
}
