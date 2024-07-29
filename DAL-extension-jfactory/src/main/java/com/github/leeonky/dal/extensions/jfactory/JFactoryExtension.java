package com.github.leeonky.dal.extensions.jfactory;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.PropertyAccessor;
import com.github.leeonky.jfactory.JFactory;

import java.util.HashSet;
import java.util.Set;

public class JFactoryExtension implements Extension {
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerPropertyAccessor(JFactory.class,
                new PropertyAccessor<JFactory>() {
                    @Override
                    public Object getValue(JFactory jFactory, Object property) {
                        return jFactory.spec((String) property).queryAll();
                    }

                    @Override
                    public Set<Object> getPropertyNames(JFactory jFactory) {
                        return new HashSet<>(jFactory.specNames());
                    }
                });
    }
}
