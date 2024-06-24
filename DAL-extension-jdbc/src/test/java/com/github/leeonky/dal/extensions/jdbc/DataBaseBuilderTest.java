package com.github.leeonky.dal.extensions.jdbc;

import lombok.SneakyThrows;
import org.hibernate.Session;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import static com.github.leeonky.dal.Assertions.expect;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataBaseBuilderTest {
    private final DataBaseBuilder builder = new DataBaseBuilder();

    @Nested
    class GlobalStrategy {

        @Nested
        class JoinColumn {

            @Test
            void register_and_apply_join_column_strategy() {
                builder.defaultStrategy().joinColumnStrategy((t1, t2) -> t1.name() + "_" + t2.name());

                expect(builder.resolveJoinColumn(givenTableWithName("t1"), givenTableWithName("t2"))).exact("t1_t2");
            }

            @Test
            void register_and_apply_referenced_column_strategy() {
                builder.defaultStrategy().referencedColumnStrategy((t1, t2) -> t1.name() + "_" + t2.name());

                expect(builder.resolveReferencedColumn(givenTableWithName("t1"), givenTableWithName("t2"))).exact("t1_t2");
            }
        }

        @Nested
        class RowMethod {

            @Test
            void register_and_apply_row_method() {
                builder.tableStrategy("table").registerRowMethod("getTableName", row1 -> row1.table().name());
                DataBase.Table<?> table = givenTableWithName("table");
                DataBase.Row row = mock(DataBase.Row.class);
                when(row.table()).thenReturn(table);

                expect(builder.callRowMethod(row, "getTableName")).exact("table");
            }
        }
    }

    @Nested
    class TableStrategy {

        @Nested
        class JoinColumn {

            @Test
            void register_and_apply_join_column_strategy() {
                builder.tableStrategy("t1").joinColumnStrategy("t2", (t1, t2) -> t1.name() + "_" + t2.name());

                expect(builder.resolveJoinColumn(givenTableWithName("t1"), givenTableWithName("t2"))).exact("t1_t2");
            }

            @Test
            void register_and_apply_join_column_strategy_with_one_table() {
                builder.tableStrategy("t1").joinColumnStrategy((t1, t2) -> t1.name() + "_" + t2.name());

                expect(builder.resolveJoinColumn(givenTableWithName("t1"), givenTableWithName("any"))).exact("t1_any");
            }

            @Test
            void register_and_apply_referenced_column_strategy() {
                builder.tableStrategy("t1").referencedColumnStrategy("t2", (t1, t2) -> t1.name() + "_" + t2.name());

                expect(builder.resolveReferencedColumn(givenTableWithName("t1"), givenTableWithName("t2"))).exact("t1_t2");
            }

            @Test
            void register_and_apply_referenced_column_strategy_with_one_table() {
                builder.tableStrategy("t1").referencedColumnStrategy((t1, t2) -> t1.name() + "_" + t2.name());

                expect(builder.resolveReferencedColumn(givenTableWithName("t1"), givenTableWithName("any"))).exact("t1_any");
            }
        }

        @Nested
        class RowMethod {

            @Test
            void register_and_apply_row_method() {
                builder.tableStrategy("table").registerRowMethod("getTableName", row -> row.table().name());

                DataBase.Table<?> table = givenTableWithName("table");
                DataBase.Row row = mock(DataBase.Row.class);
                when(row.table()).thenReturn(table);

                expect(builder.callRowMethod(row, "getTableName")).exact("table");
            }

            @Test
            void register_and_apply_default_row_method() {
                builder.registerRowMethod("getTableName", row -> row.table().name());

                DataBase.Table<?> table = givenTableWithName("table");
                DataBase.Row row = mock(DataBase.Row.class);
                when(row.table()).thenReturn(table);

                expect(builder.callRowMethod(row, "getTableName")).exact("table");
            }
        }

        @Nested
        class TableMethod {

            @Test
            void register_and_apply_table_method() {
                builder.tableStrategy("tableName").registerMethod("getTableName", DataBase.Table::name);

                expect(builder.callTableMethod(givenTableWithName("tableName"), "getTableName")).exact("tableName");
            }

            @Test
            void register_and_apply_default_table_method() {
                builder.registerMethod("getTableName", DataBase.Table::name);

                expect(builder.callTableMethod(givenTableWithName("tableName"), "getTableName")).exact("tableName");
            }
        }
    }

    @Nested
    class Provider {

        @SneakyThrows
        @Test
        void support_query_all_table_names_in_via_sql() {
            EntityManager entityManager = Persistence.createEntityManagerFactory("jfactory-repo").createEntityManager();

            builder.sqlTablesProvider("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES\n" +
                    "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_TYPE IN ('TABLE', 'VIEW');");

            Session session = entityManager.unwrap(Session.class);
            session.doWork(connection -> {
                DataBase connect = builder.connect(connection);
                expect(connect.allTableNames()).should(": [SKUS SKU_PRODUCTS PRODUCTS ORDERS ORDER_LINES]");
            });
        }
    }

    private DataBase.Table<?> givenTableWithName(String name) {
        DataBase.Table<?> table = mock(DataBase.Table.class);
        when(table.name()).thenReturn(name);
        return table;
    }
}