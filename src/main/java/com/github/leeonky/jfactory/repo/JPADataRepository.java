package com.github.leeonky.jfactory.repo;

import com.github.leeonky.jfactory.DataRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.Collection;

public class JPADataRepository implements DataRepository {

    private final EntityManager entityManager;

    public JPADataRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public <T> Collection<T> queryAll(Class<T> type) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(type);
        query.from(type);
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public void clear() {
    }

    @Override
    public void save(Object object) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(object);
        transaction.commit();
    }
}
