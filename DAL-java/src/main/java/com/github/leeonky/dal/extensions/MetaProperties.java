package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.NoSuchAccessorException;
import com.github.leeonky.util.Sneaky;

import java.util.Set;

import static com.github.leeonky.dal.runtime.DalException.extractException;
import static com.github.leeonky.dal.runtime.Order.BUILD_IN;
import static java.lang.String.format;

@Order(BUILD_IN)
public class MetaProperties implements Extension {
    private static Object size(MetaData metaData) {
        Data data = metaData.data();
        if (data.resolved().isList())
            return data.resolved().list().size();
        throw new DalRuntimeException(format("Invalid meta property `size` for: %s", data.dump()));
    }

    private static Object throw_(MetaData metaData) {
        try {
            metaData.data().instance();
            throw new AssertionError("Expecting an error to be thrown, but nothing was thrown");
        } catch (Exception e) {
            return Sneaky.get(() -> extractException(e).orElseThrow(() -> e));
        }
    }

    private static Object object_(MetaData metaData) {
        return metaData.data().instance() == null ? null : new OriginalJavaObject(metaData.data());
    }

    private static Object keys(MetaData metaData) {
        return metaData.data().resolved().fieldNames();
    }

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerMetaProperty("size", MetaProperties::size)
                .registerMetaProperty("throw", MetaProperties::throw_)
                .registerMetaProperty("object", MetaProperties::object_)
                .registerMetaProperty("keys", MetaProperties::keys)
        ;
    }

    static class OriginalJavaObject implements ProxyObject {
        private final Data data;

        public OriginalJavaObject(Data data) {
            this.data = data;
        }

        @Override
        public Object getValue(Object property) {
            try {
                Object instance = data.instance();
                return BeanClass.createFrom(instance).getPropertyValue(instance, property.toString());
            } catch (NoSuchAccessorException ignore) {
                return data.getValue(property).instance();
            }
        }

        @Override
        public Set<?> getPropertyNames() {
            return BeanClass.createFrom(data.instance()).getPropertyReaders().keySet();
        }
    }
}
