package org.testcharm.util.property;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.util.AnnotationGetter;
import org.testcharm.util.Attr;
import org.testcharm.util.BeanClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcharm.util.BeanClass.createFrom;

public class PropertyReaderTest {
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
}
