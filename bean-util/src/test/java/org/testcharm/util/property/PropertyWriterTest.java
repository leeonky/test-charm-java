package org.testcharm.util.property;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.util.Attr;
import org.testcharm.util.BeanClass;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testcharm.util.BeanClass.create;
import static org.testcharm.util.BeanClass.createFrom;

public class PropertyWriterTest {
    private BeanClass<BeanWithPubField> beanWithPubFieldBeanClass = create(BeanWithPubField.class);

    public interface Interface {
        void setValue(String value);

        String getValue();
    }

    public interface InterfaceLambda {
        void setValue(List<?> list);
    }

    public static class BeanWithPubField {
        public static int staticField = 1;
        public final int constField = 2;

        @Attr("v1")
        public int field;
        public int field2;
        public List<Long> genericField;
        @Attr("v1")
        private int field3;
        private int privateField;

        public static void setStaticSetter(int i) {
        }

        public void setGenericMethod(List<Long> list) {
        }

        @Attr("v1")
        public void setField2(int i) {
            field2 = i + 100;
        }

        public void setField3(int i) {
        }

        public void setField4(int i) {
        }
    }

    public static class SubBeanWithPubField extends BeanWithPubField {
        public int field;
    }

    public static class Bean {
        public int i;
    }

    public static class Beans {
        public Bean bean;
        public Bean[] beans = new Bean[10];

        public void setBeanSetter(Bean bean) {
        }
    }

    @Nested
    class GetSetValue {
        BeanWithPubField bean = new BeanWithPubField();

        @Test
        void set_field_value_via_anonymous_class() {
            BeanWithPubField bean = new BeanWithPubField() {
            };
            createFrom(bean).setPropertyValue(bean, "field", 100);
            assertThat(bean.field).isEqualTo(100);
        }

        @Test
        void set_property_via_interface() {
            Interface anInterface = new Interface() {
                private String value;

                @Override
                public void setValue(String value) {
                    this.value = value;
                }

                @Override
                public String getValue() {
                    return value;
                }
            };
            createFrom(anInterface).setPropertyValue(anInterface, "value", "hello");

            assertThat(anInterface.getValue()).isEqualTo("hello");
        }

        @Test
        void set_property_via_lambda() {
            InterfaceLambda lambda = List::clear;
            List<String> list = new ArrayList<String>() {{
                add("hello");
            }};
            createFrom(lambda).setPropertyValue(lambda, "value", list);
            assertThat(list).isEmpty();
        }

        @Test
        void set_property_via_anonymous_class() {
            BeanWithPubField bean = new BeanWithPubField() {
            };

            createFrom(bean).setPropertyValue(bean, "field2", 100);

            assertThat(bean.field2).isEqualTo(200);
        }

        @Test
        void raise_error_when_set_unexpected_type_value_to_field() {
            Beans beans = new Beans();

            assertThat(assertThrows(IllegalArgumentException.class, () ->
                    create(Beans.class).setPropertyValue(beans, "bean", "unexpected value")))
                    .hasMessageContaining("Can not set java.lang.String[unexpected value] to " +
                            "property org.testcharm.util.property.PropertyWriterTest$Beans.bean<org.testcharm.util.property.PropertyWriterTest$Bean>");
        }

        @Test
        void raise_error_when_set_unexpected_type_value_to_method() {
            Beans beans = new Beans();

            assertThat(assertThrows(IllegalArgumentException.class, () ->
                    create(Beans.class).setPropertyValue(beans, "beanSetter", "unexpected value")))
                    .hasMessageContaining("Can not set java.lang.String[unexpected value] to " +
                            "property org.testcharm.util.property.PropertyWriterTest$Beans.beanSetter<org.testcharm.util.property.PropertyWriterTest$Bean>");
        }

        @Test
        void raise_error_when_set_unexpected_type_value_to_collection() {
            Bean[] beans = new Bean[1];
            assertThat(assertThrows(IllegalArgumentException.class, () ->
                    create(Bean[].class).setPropertyValue(beans, "0", "unexpected value")))
                    .hasMessageContaining("Can not set java.lang.String[unexpected value] to " +
                            "property [Lorg.testcharm.util.property.PropertyWriterTest$Bean;[0]<org.testcharm.util.property.PropertyWriterTest$Bean>");
        }

        @Test
        void raise_error_when_set_null_value_to_primitive() {
            Bean bean = new Bean();

            assertThat(assertThrows(IllegalArgumentException.class, () ->
                    create(Bean.class).setPropertyValue(bean, "i", null))).hasMessageContaining("Can not set null to ");
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
            Attr annotation = beanWithPubFieldBeanClass.getPropertyWriters().get("field2").getAnnotation(Attr.class);
            assertThat(annotation.value()).isEqualTo("v1");
        }

        @Test
        void should_try_to_return_field_annotation_when_method_has_no_annotation() {
            Attr annotation = beanWithPubFieldBeanClass.getPropertyWriter("field3").getAnnotation(Attr.class);
            assertThat(annotation.value()).isEqualTo("v1");
        }

        @Test
        void should_return_null_when_no_annotation() {
            assertThat(beanWithPubFieldBeanClass.getPropertyWriter("field4").getAnnotation(Attr.class)).isNull();
        }
    }

    @Nested
    class GetGenericType {

        @Test
        void should_support_get_generic_type_from_getter_field() {
            BeanClass<?> genericType = beanWithPubFieldBeanClass.getPropertyWriter("genericField").getType();

            assertThat(genericType.getType()).isEqualTo(List.class);

            assertThat(genericType.getTypeArguments(0).get().getType()).isEqualTo(Long.class);
        }

        @Test
        void should_support_get_generic_type_from_getter_method() {
            BeanClass<?> genericType = beanWithPubFieldBeanClass.getPropertyWriter("genericMethod").getType();

            assertThat(genericType.getType()).isEqualTo(List.class);

            assertThat(genericType.getTypeArguments(0).get().getType()).isEqualTo(Long.class);
        }
    }
}
