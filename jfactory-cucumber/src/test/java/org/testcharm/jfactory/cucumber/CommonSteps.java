package org.testcharm.jfactory.cucumber;

import io.cucumber.java.Before;

import javax.persistence.EntityTransaction;

public class CommonSteps {

    @Before
    public void clearDB() {
        EntityTransaction transaction = EntityFactory.entityManager.getTransaction();
        transaction.begin();
        EntityFactory.entityManager.createQuery("delete from Order").executeUpdate();
        EntityFactory.entityManager.createNativeQuery("delete from CART_PRODUCT").executeUpdate();
        EntityFactory.entityManager.createQuery("delete from ProductStock").executeUpdate();
        EntityFactory.entityManager.createQuery("delete from Product").executeUpdate();
        EntityFactory.entityManager.createQuery("delete from Cart").executeUpdate();
        EntityFactory.entityManager.createQuery("delete from SnakeCaseProduct").executeUpdate();
        EntityFactory.entityManager.createQuery("delete from Employee").executeUpdate();
        EntityFactory.entityManager.createQuery("delete from Department").executeUpdate();
        EntityFactory.entityManager.createQuery("delete from Company").executeUpdate();
        transaction.commit();
        EntityFactory.jpaDataRepository.clear();
    }
}
