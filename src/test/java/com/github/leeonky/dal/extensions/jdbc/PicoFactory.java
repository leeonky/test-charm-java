package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.jfactory.JFactory;
import com.github.leeonky.jfactory.Spec;
import com.github.leeonky.jfactory.cucumber.JData;
import com.github.leeonky.jfactory.repo.JPADataRepository;
import com.github.leeonky.util.Classes;
import io.cucumber.core.backend.ObjectFactory;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

public class PicoFactory implements ObjectFactory {
    private final EntityManager entityManager = Persistence.createEntityManagerFactory("jfactory-repo").createEntityManager();
    private final io.cucumber.picocontainer.PicoFactory delegate = new io.cucumber.picocontainer.PicoFactory();
    private final Object jData = jData();

    @Override
    public void start() {
        delegate.start();
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public boolean addClass(Class<?> glueClass) {
        return delegate.addClass(glueClass);
    }

    @Override
    public <T> T getInstance(Class<T> glueClass) {
        if (JData.class.equals(glueClass))
            return (T) jData;
        return delegate.getInstance(glueClass);
    }

    private JData jData() {
        JFactory jFactory = new JFactory(new JPADataRepository(entityManager));
        Classes.assignableTypesOf(Spec.class, "com.github.leeonky.dal.extensions.jdbc").forEach(jFactory::register);
        return new JData(jFactory);
    }
}
