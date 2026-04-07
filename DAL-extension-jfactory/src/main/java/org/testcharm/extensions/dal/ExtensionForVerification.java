package org.testcharm.extensions.dal;

import org.testcharm.dal.DAL;
import org.testcharm.dal.runtime.*;
import org.testcharm.dal.runtime.inspector.KeyValueDumper;
import org.testcharm.jfactory.JFactory;

import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class ExtensionForVerification implements Extension {
    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder runtimeContextBuilder = dal.getRuntimeContextBuilder();
        runtimeContextBuilder.registerPropertyAccessor(JFactory.class,
                new PropertyAccessor<JFactory>() {
                    @Override
                    public Object getValue(JFactory jFactory, Object property) {
                        return new StaticAdaptiveList<>(() -> new CollectionDALCollection<>(jFactory.spec((String) property).queryAll()));
                    }

                    @Override
                    public Set<Object> getPropertyNames(JFactory jFactory) {
                        return new HashSet<>(jFactory.specNames());
                    }
                });
        runtimeContextBuilder.registerDumper(JFactory.class, data -> new KeyValueDumper<JFactory>() {

            @Override
            protected Set<?> getFieldNames(Data<JFactory> data) {
                return super.getFieldNames(data).stream().filter(p -> !data.property(p).list().isEmpty()).collect(toSet());
            }
        });
    }
}
