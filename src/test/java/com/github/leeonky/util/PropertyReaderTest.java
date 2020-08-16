package com.github.leeonky.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertyReaderTest {
    private static final int ANY_INT = 100;
    private BeanClass<BeanWithPubField> beanWithPubFieldBeanClass = BeanClass.create(BeanWithPubField.class);

    public static class BeanWithPubField {

        @Attr("v1")
        public final int field = 100;
        public final int field2 = 0;
        private final int privateField = 1;
        public List<Long> genericField;
        public List<List<Long>> nestedGenericField;
        public List notGenericField;
        @Attr("v1")
        private int field3;

        public List<Long> getGenericMethod() {
            return null;
        }

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

    public static class SubBeanWithPubField extends BeanWithPubField {
        public final int field = 200;
    }

    public static class InvalidGenericType<T> {
        public List<T> list;
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

        @Test
        void should_not_contain_java_get_class_getter() {
            assertThat(beanWithPubFieldBeanClass.getPropertyReaders().keySet()).doesNotContain("class");
        }

        @Test
        void should_override_fields_in_super_class() {
            assertThat(BeanClass.create(SubBeanWithPubField.class).getPropertyValue(new SubBeanWithPubField(), "field")).isEqualTo(200);
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

    @Nested
    class GetGenericType {

        @Test
        void should_return_empty_when_not_specify_generic_type() {
            assertThat(BeanClass.create(InvalidGenericType.class).getPropertyReader("list").getPropertyType().getTypeArguments(0))
                    .isEmpty();
        }

        @Test
        void should_support_get_generic_type_from_setter_field() {
            BeanClass<?> genericType = beanWithPubFieldBeanClass.getPropertyReader("genericField").getPropertyType();

            assertThat(genericType.getType()).isEqualTo(List.class);

            assertThat(genericType.getTypeArguments(0).get().getType()).isEqualTo(Long.class);
        }

        @Test
        void should_support_get_generic_type_from_setter_method() {
            BeanClass<?> genericType = beanWithPubFieldBeanClass.getPropertyReader("genericMethod").getPropertyType();

            assertThat(genericType.getType()).isEqualTo(List.class);

            assertThat(genericType.getTypeArguments(0).get().getType()).isEqualTo(Long.class);
        }

        @Test
        void should_support_nested_generic_parameter() {
            assertThat(beanWithPubFieldBeanClass.getPropertyReader("nestedGenericField")
                    .getPropertyType().getTypeArguments(0).get().getTypeArguments(0).get().getType()).isEqualTo(Long.class);
        }

        @Test
        void should_return_emtpy_when_type_is_not_generic() {
            BeanClass<?> genericType = beanWithPubFieldBeanClass.getPropertyReader("notGenericField").getPropertyType();

            assertThat(genericType.getType()).isEqualTo(List.class);

            assertThat(genericType.getTypeArguments(ANY_INT)).isEmpty();
        }
    }
}
