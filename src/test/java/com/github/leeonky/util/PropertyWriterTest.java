package com.github.leeonky.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyWriterTest {
    private BeanClass<BeanWithPubField> beanWithPubFieldBeanClass = new BeanClass<>(BeanWithPubField.class);

    public static class BeanWithPubField {
        @Attr("v1")
        public int field;
        public int field2;

        @Attr("v1")
        private int field3;
        private int privateField;

        @Attr("v1")
        public void setField2(int i) {
            field2 = i + 100;
        }

        public void setField3(int i) {
        }
    }

    @Nested
    class GetSetValue {

        @Test
        void set_field_value() {
            BeanWithPubField bean = new BeanWithPubField();
            beanWithPubFieldBeanClass.setPropertyValue("field", bean, 100);
            assertThat(bean.field).isEqualTo(100);
        }

        @Test
        void set_value_via_setter_override_field() {
            BeanWithPubField bean = new BeanWithPubField();
            beanWithPubFieldBeanClass.setPropertyValue("field2", bean, 100);
            assertThat(bean.field2).isEqualTo(200);
        }

        @Test
        void should_raise_error_when_no_reader() {
            assertThrows(IllegalArgumentException.class, () ->
                    beanWithPubFieldBeanClass.setPropertyValue("notExist", new BeanWithPubField(), null));
        }
    }

    @Nested
    class GetAnnotation {

        @Test
        void should_support_get_annotation_from_field() {
            Attr annotation = beanWithPubFieldBeanClass.getPropertyWriter("field").getAnnotation(Attr.class);
            assertThat(annotation.value()).isEqualTo("v1");
        }

        @Test
        void should_support_get_annotation_from_method() {
            Attr annotation = beanWithPubFieldBeanClass.getPropertyWriter("field2").getAnnotation(Attr.class);
            assertThat(annotation.value()).isEqualTo("v1");
        }

        @Test
        void should_try_to_return_field_annotation_when_method_has_no_annotation() {
            Attr annotation = beanWithPubFieldBeanClass.getPropertyWriter("field3").getAnnotation(Attr.class);
            assertThat(annotation.value()).isEqualTo("v1");
        }
    }
}
