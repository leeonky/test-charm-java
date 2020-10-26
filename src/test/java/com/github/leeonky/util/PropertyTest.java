package com.github.leeonky.util;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyTest {
    private final BeanClass<Bean> beanClass = BeanClass.create(Bean.class);

    @Accessors(chain = true)
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
            Map<String, Property<Bean>> properties = beanClass.getProperties();

            assertThat(properties).hasSize(2);
            assertThat(properties.get("str1"))
                    .hasFieldOrPropertyWithValue("name", "str1")
                    .hasFieldOrPropertyWithValue("beanType", beanClass);
            assertThat(properties.get("str2"))
                    .hasFieldOrPropertyWithValue("name", "str2")
                    .hasFieldOrPropertyWithValue("beanType", beanClass);
        }

        @Test
        void get_single_property() {
            Property<Bean> property = beanClass.getProperty("str1");

            assertThat(property)
                    .hasFieldOrPropertyWithValue("name", "str1")
                    .hasFieldOrPropertyWithValue("beanType", beanClass);
        }

        @Test
        void should_raise_error_when_no_property_with_given_name() {
            assertThrows(IllegalArgumentException.class, () -> beanClass.getProperty("notExistProperty"));
        }
    }

    @Nested
    class GetReaderWriter {

        @Test
        void get_reader_from_property() {
            assertThat(beanClass.getPropertyReader("str2")).isEqualTo(beanClass.getProperty("str2").getReader());
            assertThat(beanClass.getPropertyReader("str2").getType()).isEqualTo(beanClass.getProperty("str2").getReaderType());
        }

        @Test
        void get_writer_from_property() {
            assertThat(beanClass.getPropertyWriter("str1")).isEqualTo(beanClass.getProperty("str1").getWriter());
            assertThat(beanClass.getPropertyWriter("str1").getType()).isEqualTo(beanClass.getProperty("str1").getWriterType());
        }
    }

    @Nested
    class GetSetValue {

        @Test
        void set_value_via_property() {
            Bean bean = new Bean();
            Property<Bean> property = beanClass.getProperty("str2");

            assertThat(property.setValue(bean, "hello")).isEqualTo(property);
            assertThat(bean).hasFieldOrPropertyWithValue("str2", "hello");
        }

        @Test
        void get_value_via_property() {
            Bean bean = new Bean().setStr2("hello");

            assertThat((Object) beanClass.getProperty("str2").getValue(bean)).isEqualTo("hello");
        }
    }
}
