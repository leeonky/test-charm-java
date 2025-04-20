package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.leeonky.dal.runtime.Order.BUILD_IN;

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
                    public Data<?> getData(Data<AdaptiveList<?>> data, Object property, RuntimeContextBuilder.DALRuntimeContext context) {
                        return data.map(AdaptiveList::soloList).property(0).property(property);
                    }

                    @Override
                    public Set<?> getPropertyNames(Data<AdaptiveList<?>> data) {
                        return data.map(AdaptiveList::soloList).property(0).fieldNames();
                    }
                })
                .registerMetaPropertyPattern(AdaptiveList.class, ".*",
                        (RuntimeDataHandler<MetaData<AdaptiveList>>) metaData -> {
                            if (metaData.name().equals("size"))
                                return metaData.delegate(d -> d.map(AdaptiveList::list));
                            else {
                                return metaData.delegate(d -> d.map(AdaptiveList::soloList).property(0));
                            }
                        })
        ;
    }
}
