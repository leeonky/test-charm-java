package com.github.leeonky.jfactory.repo;

import com.github.leeonky.jfactory.JFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class JPADataRepositoryTest {
    private final EntityManager entityManager = Persistence.createEntityManagerFactory("jfactory-repo").createEntityManager();
    private final EntityManager anotherEntityManager = Persistence.createEntityManagerFactory("jfactory-repo").createEntityManager();
    private final JPADataRepository jpaDataRepository = new JPADataRepository(entityManager);

    @BeforeEach
    void clearDB() {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        entityManager.createNativeQuery("delete from bean").executeUpdate();
        entityManager.createNativeQuery("delete from compositeidbean").executeUpdate();
        transaction.commit();
    }

    @Test
    void save_should_persist_data_to_db() {
        Bean bean1 = new Bean().setId(1).setValue("hello");
        Bean bean2 = new Bean().setId(2).setValue("world");

        jpaDataRepository.save(bean1);
        jpaDataRepository.save(bean2);

        assertThat(entityManager.find(Bean.class, 1L)).isEqualTo(bean1);
        assertThat(entityManager.find(Bean.class, 2L)).isEqualTo(bean2);
    }

    @Test
    void allow_save_null() {
        assertDoesNotThrow(() -> jpaDataRepository.save(null));
    }

    @Test
    void query_all_should_get_all_data_by_type_from_db() {
        Bean bean1 = new Bean().setId(1).setValue("hello");
        Bean bean2 = new Bean().setId(2).setValue("world");

        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(bean1);
        entityManager.persist(bean2);
        transaction.commit();

        assertThat(jpaDataRepository.queryAll(Bean.class))
                .containsExactly(bean1, bean2);
    }

    @Test
    void should_clear_cache_before_query() {
        Bean bean = new Bean().setId(1).setValue("hello");
        jpaDataRepository.save(bean);

        EntityTransaction transaction = anotherEntityManager.getTransaction();
        transaction.begin();
        anotherEntityManager.persist(anotherEntityManager.find(Bean.class, 1L).setValue("world"));
        transaction.commit();

        assertThat(jpaDataRepository.queryAll(Bean.class).iterator().next())
                .hasFieldOrPropertyWithValue("value", "world");
    }

    @Test
    void should_clear_entity_manager_when_clear_repo() {
        Bean bean = new Bean().setId(1).setValue("hello");
        jpaDataRepository.save(bean);
        jpaDataRepository.clear();

        assertNotSame(entityManager.find(Bean.class, 1L), bean);
    }

    @Nested
    class CompositeId_ {
        JFactory jFactory = new JFactory(jpaDataRepository);

        @Test
        void skip_save_composite_id_object() {
            CompositeIdBean compositeIdBean = jFactory.type(CompositeIdBean.class)
                    .property("id.key1", "k1")
                    .property("id.key2", "k2")
                    .property("value", "string")
                    .create();

            assertThat(compositeIdBean)
                    .hasFieldOrPropertyWithValue("value", "string");

            assertThat(compositeIdBean.getId())
                    .hasFieldOrPropertyWithValue("key1", "k1")
                    .hasFieldOrPropertyWithValue("key2", "k2");
        }

        @Test
        void create_composite_id_but_not_save() {
            CompositeId compositeId = jFactory.create(CompositeId.class);

            assertThat(compositeId.getKey1()).isInstanceOf(String.class);
            assertThat(compositeId.getKey2()).isInstanceOf(String.class);

            assertThat(jFactory.type(CompositeId.class).queryAll()).isEmpty();
        }
    }
}