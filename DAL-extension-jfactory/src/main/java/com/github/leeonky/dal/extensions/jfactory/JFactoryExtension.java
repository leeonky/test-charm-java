package com.github.leeonky.dal.extensions.jfactory;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;
import com.github.leeonky.dal.runtime.inspector.KeyValueDumper;
import com.github.leeonky.jfactory.JFactory;

import java.util.HashSet;
import java.util.Set;

public class JFactoryExtension implements Extension {
    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder runtimeContextBuilder = dal.getRuntimeContextBuilder();
        runtimeContextBuilder.registerPropertyAccessor(JFactory.class,
                new PropertyAccessor<JFactory>() {
                    @Override
                    public Object getValue(JFactory jFactory, Object property) {
                        return AdaptiveList.staticList(jFactory.spec((String) property).queryAll());
                    }

                    @Override
                    public Set<Object> getPropertyNames(JFactory jFactory) {
                        return new HashSet<>(jFactory.specNames());
                    }
                });
        runtimeContextBuilder.registerDumper(JFactory.class, data -> new KeyValueDumper<JFactory>() {
            @Override
            protected void dumpField(Data<JFactory> data, Object field, DumpingBuffer context) {
                try {
                    Data<?> value = data.property(field);
                    if (value.list().size() != 0) {
                        context.append(key(field)).append(": ");
                        context.dumpValue(value);
                    }
                } catch (Throwable e) {
                    context.append(key(field)).append(": ").append(e);
                }
            }
        });
    }
}
