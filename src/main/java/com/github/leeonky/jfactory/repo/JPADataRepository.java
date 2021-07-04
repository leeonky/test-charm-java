package com.github.leeonky.jfactory.repo;

import com.github.leeonky.jfactory.DataRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaQuery;
import java.util.Collection;

public class JPADataRepository implements DataRepository {

    private final EntityManager entityManager;

    public JPADataRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public <T> Collection<T> queryAll(Class<T> type) {
        CriteriaQuery<T> query = entityManager.getCriteriaBuilder().createQuery(type);
        query.from(type);
        entityManager.clear();
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public void clear() {
        entityManager.clear();
    }

    @Override
    public void save(Object object) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(object);
        transaction.commit();
    }
}
