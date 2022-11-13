package com.github.leeonky;

import com.github.leeonky.util.AnnotationGetter;
import com.github.leeonky.util.Attr;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.NoSuchAccessorException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import static com.github.leeonky.util.BeanClass.createFrom;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertyReaderTest {
    private static final int ANY_INT = 100;
    private BeanClass<BeanWithPubField> beanWithPubFieldBeanClass = BeanClass.create(BeanWithPubField.class);

    public interface Interface {
        void setValue(String value);

        String getValue();
    }

    public interface InterfaceLambda {
        String getValue();
    }

    public static class BeanWithPubField {

        public static int staticField = 1;
        @Attr("v1")
        public final int field = 100;
        public final int field2 = 0;
        private final int privateField = 1;
        public List<Long> genericField;
        public List<List<Long>> nestedGenericField;
        public List notGenericField;
        @Attr("v1")
        private int field3;

        public static int getStaticGetter() {
            return 0;
        }

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
    class GetParentType {

        @Test
        void get_bean_class() {
            assertThat(beanWithPubFieldBeanClass.getPropertyReader("field").getBeanType()).isEqualTo(beanWithPubFieldBeanClass);
        }
    }

    @Nested
    class GetSetValue {

        @Test
        void get_field_value() {
            assertThat(beanWithPubFieldBeanClass.getPropertyValue(new BeanWithPubField(), "field")).isEqualTo(100);
        }

        @Test
        void get_field_value_of_anonymous_class() {
            BeanWithPubField bean = new BeanWithPubField() {
            };
            assertThat(createFrom(bean).getPropertyValue(bean, "field")).isEqualTo(100);
        }

        @Test
        void get_property_value_of_interface_instance() {
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
            anInterface.setValue("hello");

            assertThat(createFrom(anInterface).getPropertyValue(anInterface, "value")).isEqualTo("hello");
        }

        @Test
        void get_value_via_getter_override_field() {
            assertThat(beanWithPubFieldBeanClass.getPropertyValue(new BeanWithPubField(), "field2")).isEqualTo(200);
        }

        @Test
        void get_property_via_interface() {
            InterfaceLambda lambda = () -> "hello";

            Object value = createFrom(lambda).getPropertyValue(lambda, "value");
            assertThat(value).isEqualTo("hello");
        }

        @Test
        void get_value_via_getter_of_anonymous_class() {
            BeanWithPubField bean = new BeanWithPubField() {
            };

            assertThat(createFrom(bean).getPropertyValue(bean, "field2")).isEqualTo(200);
        }

        @Test
        void should_support_boolean_getter() {
            assertTrue((Boolean) beanWithPubFieldBeanClass.getPropertyValue(new BeanWithPubField(), "bool"));
        }

        @Test
        void should_raise_error_when_no_reader() {
            assertThrows(NoSuchAccessorException.class, () ->
                    beanWithPubFieldBeanClass.getPropertyValue(new BeanWithPubField(), "boolean"));

            assertThrows(NoSuchAccessorException.class, () ->
                    beanWithPubFieldBeanClass.getPropertyValue(new BeanWithPubField(), "privateField"));
        }

        @Test
        void should_not_contain_java_get_class_getter() {
            assertThat(beanWithPubFieldBeanClass.getPropertyReaders().keySet()).doesNotContain("class");
        }

        @Test
        void should_not_contain_static_field() {
            assertThat(beanWithPubFieldBeanClass.getPropertyReaders().keySet()).doesNotContain("staticField");
        }

        @Test
        void should_not_contain_static_getter() {
            assertThat(beanWithPubFieldBeanClass.getPropertyReaders().keySet()).doesNotContain("staticGetter");
        }

        @Test
        void should_override_fields_in_super_class() {
            assertThat(BeanClass.create(SubBeanWithPubField.class).getPropertyValue(new SubBeanWithPubField(), "field")).isEqualTo(200);
        }

        @Test
        void should_support_get_class_value() {
            assertThat(beanWithPubFieldBeanClass.getPropertyValue(new BeanWithPubField(), "class"))
                    .isEqualTo(BeanWithPubField.class);

            assertThat(beanWithPubFieldBeanClass.getPropertyChainValue(new BeanWithPubField(), "class.simpleName"))
                    .isEqualTo("BeanWithPubField");
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
            assertThat(beanWithPubFieldBeanClass.getPropertyReader("field2").getAnnotation(Attr.class).value()).isEqualTo("v1");
            assertThat(beanWithPubFieldBeanClass.getPropertyReader("field2").annotation(Attr.class).get().value()).isEqualTo("v1");
        }

        @Test
        void should_try_to_return_field_annotation_when_method_has_no_annotation() {
            Attr annotation = beanWithPubFieldBeanClass.getPropertyReader("field3").getAnnotation(Attr.class);
            assertThat(annotation.value()).isEqualTo("v1");
        }

        @Test
        void support_use_customer_annotation_getter() {
            AnnotationGetter.setAnnotationGetter(new AnnotationGetter() {
                @Override
                public <A extends Annotation> A getAnnotation(Field field, Class<A> annotationClass) {
                    return null;
                }
            });

            assertThat(beanWithPubFieldBeanClass.getPropertyReader("field3").getAnnotation(Attr.class)).isNull();
            assertThat(beanWithPubFieldBeanClass.getPropertyReader("field3").annotation(Attr.class)).isEmpty();
            AnnotationGetter.setAnnotationGetter(new AnnotationGetter());
        }
    }

    @Nested
    class GetGenericType {

        @Test
        void should_return_empty_when_not_specify_generic_type() {
            assertThat(BeanClass.create(InvalidGenericType.class).getPropertyReader("list").getType().getTypeArguments(0))
                    .isEmpty();
        }

        @Test
        void should_support_get_generic_type_from_setter_field() {
            BeanClass<?> genericType = beanWithPubFieldBeanClass.getPropertyReader("genericField").getType();

            assertThat(genericType.getType()).isEqualTo(List.class);

            assertThat(genericType.getTypeArguments(0).get().getType()).isEqualTo(Long.class);
        }

        @Test
        void should_support_get_generic_type_from_setter_method() {
            BeanClass<?> genericType = beanWithPubFieldBeanClass.getPropertyReader("genericMethod").getType();

            assertThat(genericType.getType()).isEqualTo(List.class);

            assertThat(genericType.getTypeArguments(0).get().getType()).isEqualTo(Long.class);
        }

        @Test
        void should_support_nested_generic_parameter() {
            assertThat(beanWithPubFieldBeanClass.getPropertyReader("nestedGenericField")
                    .getType().getTypeArguments(0).get().getTypeArguments(0).get().getType()).isEqualTo(Long.class);
        }

        @Test
        void should_return_emtpy_when_type_is_not_generic() {
            BeanClass<?> genericType = beanWithPubFieldBeanClass.getPropertyReader("notGenericField").getType();

            assertThat(genericType.getType()).isEqualTo(List.class);

            assertThat(genericType.getTypeArguments(ANY_INT)).isEmpty();
        }
    }
}
