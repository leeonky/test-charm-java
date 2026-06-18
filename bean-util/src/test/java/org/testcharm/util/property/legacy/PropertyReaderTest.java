package org.testcharm.util.property.legacy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.util.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testcharm.util.BeanClass.create;
import static org.testcharm.util.BeanClass.createFrom;

public class PropertyReaderTest {
    private static final int ANY_INT = 100;
    private BeanClass<BeanWithPubField> beanWithPubFieldBeanClass = create(BeanWithPubField.class);

    public interface Interface {
        void setValue(String value);

        String getValue();
    }

    public interface InterfaceLambda {
        String getValue();
    }

    public static class Bean {
        public int i;
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

    @Nested
    class Decorator {
        PropertyReader<?> reader = mock(PropertyReader.class);
        PropertyReaderDecorator<?> decorator = new PropertyReaderDecorator<>(reader);

        @Nested
        class Forwarding {
            @Test
            void should_forward_get_name() {
                when(reader.getName()).thenReturn("propertyName");

                assertEquals("propertyName", decorator.getName());
            }

            @Test
            void should_forward_get_annotation() {
                Attr annotation = mock(Attr.class);
                when(reader.getAnnotation(Attr.class)).thenReturn(annotation);

                assertEquals(annotation, decorator.getAnnotation(Attr.class));
            }

            @Test
            void should_forward_isBeanProperty() {
                when(reader.isBeanProperty()).thenReturn(true);

                assertThat(decorator.isBeanProperty()).isTrue();
            }

            @Test
            void should_forward_getBeanType() {
                BeanClass<?> beanType = create(String.class);
                when(reader.getBeanType()).thenReturn(Sneaky.cast(beanType));

                assertEquals(beanType, decorator.getBeanType());
            }

            @Test
            void should_forward_getGenericType() {
                Type genericType = mock(Type.class);
                when(reader.getGenericType()).thenReturn(genericType);

                assertEquals(genericType, decorator.getGenericType());
            }

            @Test
            void should_forward_getOriginType() {
                BeanClass<?> originType = create(String.class);
                when(reader.getOriginType()).thenReturn(Sneaky.cast(originType));

                assertEquals(originType, decorator.getOriginType());
            }

            @Test
            void should_forwarding_getValue() {
                Object value = new Object();
                when(reader.getValue(any())).thenReturn(value);
                Object instance = new Object();

                assertEquals(value, decorator.getValue(Sneaky.cast(instance)));

                verify(reader).getValue(Sneaky.cast(instance));
            }

            @Test
            void should_forwarding_getPropertyChainReader() {
                PropertyReader<?> chainReader = mock(PropertyReader.class);
                when(reader.getPropertyChainReader(any())).thenReturn(Sneaky.cast(chainReader));

                List<Object> chain = new ArrayList<>();

                assertEquals(chainReader, decorator.getPropertyChainReader(chain));

                verify(reader).getPropertyChainReader(chain);
            }
        }

        @Nested
        class Overriding {

            @Test
            void getType_should_return_from_self_getGenericType() {
                when(reader.getType()).thenThrow(new RuntimeException());

                Type genericType = new ArrayList<String>() {
                }.getClass().getGenericSuperclass();
                when(reader.getGenericType()).thenReturn(genericType);

                assertEquals(genericType, decorator.getType().getGenericType());
            }

            @Test
            void tryConvert_should_use_self_getType() {
                when(reader.getType()).thenReturn(Sneaky.cast(create(Long.class)));

                PropertyReaderDecorator<?> decorator = new PropertyReaderDecorator(reader) {
                    @Override
                    public BeanClass getType() {
                        return create(Integer.class);
                    }
                };

                assertEquals(100, decorator.tryConvert("100"));
            }
        }
    }
}
