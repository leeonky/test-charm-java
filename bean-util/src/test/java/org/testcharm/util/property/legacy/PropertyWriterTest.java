package org.testcharm.util.property.legacy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.util.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
                            "property org.testcharm.util.property.legacy.PropertyWriterTest$Beans.bean<org.testcharm.util.property.legacy.PropertyWriterTest$Bean>");
        }

        @Test
        void raise_error_when_set_unexpected_type_value_to_method() {
            Beans beans = new Beans();

            assertThat(assertThrows(IllegalArgumentException.class, () ->
                    create(Beans.class).setPropertyValue(beans, "beanSetter", "unexpected value")))
                    .hasMessageContaining("Can not set java.lang.String[unexpected value] to " +
                            "property org.testcharm.util.property.legacy.PropertyWriterTest$Beans.beanSetter<org.testcharm.util.property.legacy.PropertyWriterTest$Bean>");
        }

        @Test
        void raise_error_when_set_unexpected_type_value_to_collection() {
            Bean[] beans = new Bean[1];
            assertThat(assertThrows(IllegalArgumentException.class, () ->
                    create(Bean[].class).setPropertyValue(beans, "0", "unexpected value")))
                    .hasMessageContaining("Can not set java.lang.String[unexpected value] to " +
                            "property [Lorg.testcharm.util.property.legacy.PropertyWriterTest$Bean;[0]<org.testcharm.util.property.legacy.PropertyWriterTest$Bean>");
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

    @Nested
    class Decorator {
        PropertyWriter<?> writer = mock(PropertyWriter.class);
        PropertyWriterDecorator<?> decorator = new PropertyWriterDecorator<>(writer);

        @Nested
        class Forwarding {
            @Test
            void should_forward_get_name() {
                when(writer.getName()).thenReturn("propertyName");

                assertEquals("propertyName", decorator.getName());
            }

            @Test
            void should_forward_get_annotation() {
                Attr annotation = mock(Attr.class);
                when(writer.getAnnotation(Attr.class)).thenReturn(annotation);

                assertEquals(annotation, decorator.getAnnotation(Attr.class));
            }

            @Test
            void should_forward_isBeanProperty() {
                when(writer.isBeanProperty()).thenReturn(true);

                assertThat(decorator.isBeanProperty()).isTrue();
            }

            @Test
            void should_forward_getBeanType() {
                BeanClass<?> beanType = create(String.class);
                when(writer.getBeanType()).thenReturn(Sneaky.cast(beanType));

                assertEquals(beanType, decorator.getBeanType());
            }

            @Test
            void should_forward_getGenericType() {
                Type genericType = mock(Type.class);
                when(writer.getGenericType()).thenReturn(genericType);

                assertEquals(genericType, decorator.getGenericType());
            }

            @Test
            void should_forward_getOriginType() {
                BeanClass<?> originType = create(String.class);
                when(writer.getOriginType()).thenReturn(Sneaky.cast(originType));

                assertEquals(originType, decorator.getOriginType());
            }
        }

        @Nested
        class Overriding {

            @Test
            void getType_should_return_from_self_getGenericType() {
                when(writer.getType()).thenThrow(new RuntimeException());

                Type genericType = new ArrayList<String>() {
                }.getClass().getGenericSuperclass();
                when(writer.getGenericType()).thenReturn(genericType);

                assertEquals(genericType, decorator.getType().getGenericType());
            }

            @Test
            void tryConvert_should_use_self_getType() {
                when(writer.getType()).thenReturn(Sneaky.cast(BeanClass.create(Long.class)));

                PropertyWriterDecorator<?> decorator = new PropertyWriterDecorator(writer) {
                    @Override
                    public BeanClass getType() {
                        return BeanClass.create(Integer.class);
                    }
                };

                assertEquals(100, decorator.tryConvert("100"));
            }
        }

        @Nested
        class SetValue {

            @Test
            void set_value_from_decorator_writer() {
                Bean bean = new Bean();
                PropertyWriter<Bean> writer = create(Bean.class).getPropertyWriter("i");
                PropertyWriterDecorator<Bean> decorator = new PropertyWriterDecorator<>(writer);

                decorator.setValue(bean, 100);

                assertThat(bean.i).isEqualTo(100);
            }

            @Test
            void set_value_via_self_tryConvert() {
                Bean bean = new Bean();
                PropertyWriter<Bean> writer = create(Bean.class).getPropertyWriter("i");
                PropertyWriterDecorator<Bean> decorator = new PropertyWriterDecorator(writer) {
                    @Override
                    public Object tryConvert(Object value) {
                        return (int) value + 100;
                    }
                };

                decorator.setValue(bean, 100);

                assertThat(bean.i).isEqualTo(200);
            }

            @Test
            void forwarding_CannotSetElementByIndexException() {
                PropertyWriter<Bean> writer = create(Bean.class).getPropertyWriter("i");
                CannotSetElementByIndexException cannotSetElementByIndexException = new CannotSetElementByIndexException(Object.class);
                PropertyWriterDecorator<?> decorator = new PropertyWriterDecorator(writer) {
                    @Override
                    public BiConsumer setter() {
                        return (o, o2) -> {
                            throw cannotSetElementByIndexException;
                        };
                    }
                };

                CannotSetElementByIndexException thrown = assertThrows(CannotSetElementByIndexException.class, () ->
                        decorator.setValue(Sneaky.cast(new Object()), new Object()));

                assertEquals(cannotSetElementByIndexException, thrown);
            }

            @Test
            void forwarding_IllegalArgumentException() {
                PropertyWriter<Bean> writer = create(Bean.class).getPropertyWriter("i");
                IllegalArgumentException illegalArgumentException = new IllegalArgumentException();
                PropertyWriterDecorator<Bean> decorator = new PropertyWriterDecorator<Bean>(writer) {
                    @Override
                    public BiConsumer<Bean, Object> setter() {
                        return (o, o2) -> {
                            throw illegalArgumentException;
                        };
                    }
                };

                IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                        decorator.setValue(new Bean(), 100));

                assertEquals(illegalArgumentException, thrown.getCause());
                assertEquals("Can not set java.lang.Integer[100] to property org.testcharm.util.property.legacy.PropertyWriterTest$Bean.i<int>", thrown.getMessage());
            }
        }
    }
}
