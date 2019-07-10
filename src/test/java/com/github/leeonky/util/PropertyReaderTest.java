package com.github.leeonky.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertyReaderTest {
    BeanClass<BeanWithPubField> beanWithPubFieldBeanClass = new BeanClass<>(BeanWithPubField.class);

    public static class BeanWithPubField {

        @Attr("v1")
        public final int field = 100;
        public final int field2 = 0;
        private final int privateField = 1;

        @Attr("v1")
        private int field3;

        @Attr("v1")
        public int getField2() {
            return 200;
        }

        public boolean isBool() {
            return true;
        }

        public Boolean isBoolean() {
            return true;
        }

        public int getField3() {
            return field3;
        }
    }

    @Nested
    class GetSetValue {

        @Test
        void get_field_value() {
            assertThat(beanWithPubFieldBeanClass.getPropertyValue(new BeanWithPubField(), "field")).isEqualTo(100);
        }

        @Test
        void get_value_via_getter_override_field() {
            assertThat(beanWithPubFieldBeanClass.getPropertyValue(new BeanWithPubField(), "field2")).isEqualTo(200);
        }

        @Test
        void should_support_boolean_getter() {
            assertTrue((Boolean) beanWithPubFieldBeanClass.getPropertyValue(new BeanWithPubField(), "bool"));
        }

        @Test
        void should_raise_error_when_no_reader() {
            assertThrows(IllegalArgumentException.class, () ->
                    beanWithPubFieldBeanClass.getPropertyValue(new BeanWithPubField(), "boolean"));

            assertThrows(IllegalArgumentException.class, () ->
                    beanWithPubFieldBeanClass.getPropertyValue(new BeanWithPubField(), "privateField"));
        }
    }

    @Nested
    class GetAnnotation {

        @Test
        void should_support_get_annotation_from_field() {
            Attr annotation = beanWithPubFieldBeanClass.getPropertyReader("field").getAnnotation(Attr.class);
            assertThat(annotation.value()).isEqualTo("v1");
        }

        @Test
        void should_support_get_annotation_from_method() {
            Attr annotation = beanWithPubFieldBeanClass.getPropertyReader("field2").getAnnotation(Attr.class);
            assertThat(annotation.value()).isEqualTo("v1");
        }

        @Test
        void should_try_to_return_field_annotation_when_method_has_no_annotation() {
            Attr annotation = beanWithPubFieldBeanClass.getPropertyReader("field3").getAnnotation(Attr.class);
            assertThat(annotation.value()).isEqualTo("v1");
        }
    }
}
