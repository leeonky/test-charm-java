package com.github.leeonky.jfactory;

import com.github.leeonky.dal.extension.assertj.DALAssert;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.leeonky.dal.extension.assertj.DALAssert.expect;
import static com.github.leeonky.jfactory.TablePropertyValue.table;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TablePropertyValueTest {
    private final JFactory jFactory = new JFactory();

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Item {
        private String value, value2;
    }

    public static class AnItem extends Spec<Item> {

        @Override
        public void main() {
            property("value").value("spec");
        }
    }

    @Getter
    @Setter
    public static class Bean {
        private List<Item> list;
    }

    @Nested
    class Table {
        private final Builder<Bean> builder = jFactory.type(Bean.class);

        @Test
        void table_with_only_header_equals_empty_property() {
            expectTable("| value |").match("[]");
        }

        @Test
        void table_1_x_1() {
            expectTable("| value |\n" +
                    "| hello |")
                    .should("value: ['hello']");
        }

        @Test
        void table_2_x_1() {
            expectTable("| value |\n" +
                    "| hello |\n" +
                    "| world |")
                    .should("value: ['hello' 'world']");
        }

        @Test
        void table_2_x_2() {
            expectTable("| value | value2 |\n" +
                    "| hello | Tom |\n" +
                    "| world | Jerry |")
                    .should("value: ['hello' 'world']")
                    .should("value2: ['Tom' 'Jerry']");
        }

        @Test
        void invalid_table_too_many_cells() {
            assertThat(assertThrows(IllegalArgumentException.class, () ->
                    builder.propertyValue("list", table("| value |\n" +
                            "| hello | world |")))).hasMessage("Invalid table at row: 0, different size of cells and headers.");
        }

        @Test
        void table_with_spec() {
            jFactory.register(AnItem.class);
           
            expectTable("   | value2 |\n" +
                    "AnItem | Tom    |")
                    .should("value: ['spec']")
                    .should("value2: ['Tom']");
        }

        private DALAssert expectTable(String table) {
            return expect(builder.propertyValue("list", table(table)).create().getList());
        }
    }
}