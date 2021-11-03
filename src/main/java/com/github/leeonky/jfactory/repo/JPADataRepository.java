package com.github.leeonky.jfactory.repo;

import com.github.leeonky.jfactory.DataRepository;

import javax.persistence.Embeddable;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaQuery;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptyList;

public class JPADataRepository implements DataRepository {
    private final EntityManager entityManager;
    private final Set<Class<?>> ignoreSavingClasses = new HashSet<>();

    public JPADataRepository(EntityManager entityManager) {
        this(entityManager, emptyList());
    }

    public JPADataRepository(EntityManager entityManager, Collection<Class<?>> ignoreSavingClasses) {
        this.entityManager = entityManager;
        this.ignoreSavingClasses.addAll(ignoreSavingClasses);
    }

    @Override
    public <T> Collection<T> queryAll(Class<T> type) {
        if (isEntity(type)) {
            CriteriaQuery<T> query = entityManager.getCriteriaBuilder().createQuery(type);
            query.from(type);
            entityManager.clear();
            return entityManager.createQuery(query).getResultList();
        }
        return emptyList();
    }

    private boolean isEntity(Class<?> type) {
        return type.getAnnotation(Embeddable.class) == null && !ignoreSavingClasses.contains(type);
    }

    @Override
    public void clear() {
        entityManager.clear();
    }

    @Override
    public void save(Object object) {
        if (object != null && isEntity(object.getClass())) {
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(object);
            transaction.commit();
        }
    }
}
