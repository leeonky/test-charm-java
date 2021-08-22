package com.github.leeonky.jfactory.repo;

import com.github.leeonky.jfactory.DataRepository;

import javax.persistence.Embeddable;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaQuery;
import java.util.Collection;

import static java.util.Collections.emptyList;

public class JPADataRepository implements DataRepository {

    private final EntityManager entityManager;

    public JPADataRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public <T> Collection<T> queryAll(Class<T> type) {
        if (type.getAnnotation(Embeddable.class) == null) {
            CriteriaQuery<T> query = entityManager.getCriteriaBuilder().createQuery(type);
            query.from(type);
            entityManager.clear();
            return entityManager.createQuery(query).getResultList();
        }
        return emptyList();
    }

    @Override
    public void clear() {
        entityManager.clear();
    }

    @Override
    public void save(Object object) {
        if (object != null && object.getClass().getAnnotation(Embeddable.class) == null) {
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(object);
            transaction.commit();
        }
    }
}
