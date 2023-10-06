package com.github.leeonky.dal.extensions.jdbc;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
                builder.joinColumnStrategy((t1, t2) -> t1.name() + "_" + t2.name());

                expect(builder.resolveJoinColumn(givenTableWithName("t1"), givenTableWithName("t2"))).exact("t1_t2");
            }

            @Test
            void register_and_apply_referenced_column_strategy() {
                builder.referencedColumnStrategy((t1, t2) -> t1.name() + "_" + t2.name());

                expect(builder.resolveReferencedColumn(givenTableWithName("t1"), givenTableWithName("t2"))).exact("t1_t2");
            }
        }
    }

    private DataBase.Table<?> givenTableWithName(String name) {
        DataBase.Table<?> table = mock(DataBase.Table.class);
        when(table.name()).thenReturn(name);
        return table;
    }
}