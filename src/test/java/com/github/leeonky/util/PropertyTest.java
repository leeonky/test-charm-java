package com.github.leeonky.util;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyTest {

    public static class Bean {
        @Setter
        private String str1;

        @Setter
        @Getter
        private String str2;
    }

    @Nested
    class GetPropertyList {

        @Test
        void should_return_all_reader_and_writer_and_uniq_name() {
            Map<String, Property<Bean>> properties = BeanClass.create(Bean.class).getProperties();

            assertThat(properties).hasSize(2);
            assertThat(properties.get("str1"))
                    .hasFieldOrPropertyWithValue("name", "str1")
                    .hasFieldOrPropertyWithValue("beanType", BeanClass.create(Bean.class));
            assertThat(properties.get("str2"))
                    .hasFieldOrPropertyWithValue("name", "str2")
                    .hasFieldOrPropertyWithValue("beanType", BeanClass.create(Bean.class));
        }

        @Test
        void get_single_property() {
            Property<Bean> property = BeanClass.create(Bean.class).getProperty("str1");

            assertThat(property)
                    .hasFieldOrPropertyWithValue("name", "str1")
                    .hasFieldOrPropertyWithValue("beanType", BeanClass.create(Bean.class));
        }

        @Test
        void should_raise_error_when_no_property_with_given_name() {
            assertThrows(IllegalArgumentException.class, () -> BeanClass.create(Bean.class).getProperty("notExistProperty"));
        }
    }
}
