package com.github.leeonky.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyWriterTest {
    private BeanClass<BeanWithPubField> beanWithPubFieldBeanClass = BeanClass.create(BeanWithPubField.class);

    public static class BeanWithPubField {
        @Attr("v1")
        public int field;
        public int field2;
        public List<Long> genericField;
        @Attr("v1")
        private int field3;
        private int privateField;

        public void setGenericMethod(List<Long> list) {
        }

        @Attr("v1")
        public void setField2(int i) {
            field2 = i + 100;
        }

        public void setField3(int i) {
        }
    }

    @Nested
    class GetSetValue {
        BeanWithPubField bean = new BeanWithPubField();

        @Test
        void set_field_value() {
            beanWithPubFieldBeanClass.setPropertyValue(bean, "field", 100);
            assertThat(bean.field).isEqualTo(100);
        }

        @Test
        void set_value_via_setter_override_field() {
            beanWithPubFieldBeanClass.setPropertyValue(bean, "field2", 100);
            assertThat(bean.field2).isEqualTo(200);
        }

        @Test
        void should_raise_error_when_no_reader() {
            assertThrows(IllegalArgumentException.class, () ->
                    beanWithPubFieldBeanClass.setPropertyValue(new BeanWithPubField(), "notExist", null));
        }

        @Test
        void should_support_type_convert() {
            beanWithPubFieldBeanClass.setPropertyValue(bean, "field", "100");

            assertThat(bean.field).isEqualTo(100);

            beanWithPubFieldBeanClass.setPropertyValue(bean, "field2", "100");

            assertThat(bean.field2).isEqualTo(200);
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

    @Nested
    class GetGenericType {

        @Test
        void should_support_get_generic_type_from_getter_field() {
            GenericType genericType = beanWithPubFieldBeanClass.getPropertyWriter("genericField").getGenericType();

            assertThat(genericType.getRawType()).isEqualTo(List.class);

            assertThat(genericType.getGenericTypeParameter(0).get().getRawType()).isEqualTo(Long.class);
        }

        @Test
        void should_support_get_generic_type_from_getter_method() {
            GenericType genericType = beanWithPubFieldBeanClass.getPropertyWriter("genericMethod").getGenericType();

            assertThat(genericType.getRawType()).isEqualTo(List.class);

            assertThat(genericType.getGenericTypeParameter(0).get().getRawType()).isEqualTo(Long.class);
        }
    }
}
