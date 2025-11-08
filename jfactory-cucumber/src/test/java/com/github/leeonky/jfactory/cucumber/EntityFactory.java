package com.github.leeonky.jfactory.cucumber;

import com.github.leeonky.dal.extensions.jdbc.DataBaseBuilder;
import com.github.leeonky.jfactory.JFactory;
import com.github.leeonky.jfactory.cucumber.factory.*;
import com.github.leeonky.jfactory.repo.JPADataRepository;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Persistence;
import java.util.Collection;

import static java.util.Arrays.asList;

public class EntityFactory extends JFactory {
    public static final EntityManager entityManager = Persistence.createEntityManagerFactory("jfactory-repo").createEntityManager();
    public static final JPADataRepository jpaDataRepository = new JPADataRepository(entityManager) {
        @Override
        public <T> Collection<T> queryAll(Class<T> type) {
            if (type.equals(com.github.leeonky.dal.extensions.jdbc.DataBase.class)) {
                DataBaseBuilder builder = new DataBaseBuilder();
                return (Collection<T>) asList(builder.connect(
                        entityManager.unwrap(Session.class).doReturningWork(x -> x)));
            }
            return super.queryAll(type);
        }
    };

    public static EntityFactory runtimeInstance;

    public EntityFactory() {
        super(jpaDataRepository);
        runtimeInstance = this;

        register(Products.商品.class);
        register(Carts.购物车.class);
        register(ProductStocks.库存.class);
        register(Orders.订单.class);
        register(SnakeCaseProducts.SnakeCase商品.class);

        register(Products.ProductFactory.class);
        register(Carts.CartProduct.class);
        register(ProductStocks.Inventory.class);
        register(Orders.OrderFactory.class);

        register(Association.Company.class);
        register(Association.Department.class);
        register(Association.Employee.class);

        register(DataBase.class);

        ignoreDefaultValue(propertyWriter -> propertyWriter.getAnnotation(Id.class) != null);

        setSequenceStart(99999);
    }
}
