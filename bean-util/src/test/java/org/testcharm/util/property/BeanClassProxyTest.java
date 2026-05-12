package org.testcharm.util.property;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.util.BeanClass;
import org.testcharm.util.BeanClassProxy;
import org.testcharm.util.Property;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.testcharm.util.JavaExecutor.executor;

class BeanClassProxyTest {

    @BeforeEach
    void reset() {
        executor().main().importDependency("org.testcharm.util.BeanClass");
    }

    @Nested
    class NormalClass {

        private BeanClass<Object> beanClass;

        @BeforeEach
        void prepareClass() {
            givenClass(String.join("\n",
                    "public class Clazz {",
                    "    public int field = 100;",
                    "}"
            ));
            beanClass = createBeanClass("Clazz");
        }

        @Test
        void should_create_by_bean_class_create() {
            assertTrue(beanClass == BeanClass.create(beanClass.getType()));
        }
    }

    @Nested
    class Interface {

        @Nested
        class SingleInterfaceWithGetter {

            private BeanClass<Object> beanClass;

            @BeforeEach
            void prepareClass() {
                givenClass(String.join("\n",
                        "public interface IClazz {",
                        "    public int getField();",
                        "}"
                ));
                beanClass = createBeanClass("IClazz");
            }

            @Test
            void auto_populate_writer() {
                Map<String, Property<Object>> properties = beanClass.getProperties();

                assertThat(properties.keySet()).containsExactly("field");

                Property<?> property = properties.get("field");
                assertEquals("field", property.getName());
                assertEquals(beanClass, property.getBeanType());
                assertEquals(int.class, property.getWriterType().getType());
                assertEquals(int.class, property.getReaderType().getType());
            }

            @Test
            void get_default_primitive_value_before_set() {
                Object object = beanClass.newInstance();
                Property<Object> property = beanClass.getProperty("field");

                assertEquals(0, (int) property.getValue(object));
            }

            @Test
            void set_get_value() {
                Object object = beanClass.newInstance();
                Property<Object> property = beanClass.getProperty("field");

                property.setValue(object, 100);
                assertEquals(100, (int) property.getValue(object));

                beanClass.setPropertyValue(object, "field", 1000);

                assertEquals(1000, (int) beanClass.getPropertyValue(object, "field"));
            }

            @Test
            void object_equals() {
                Object object1 = beanClass.newInstance();
                Object object2 = beanClass.newInstance();

                assertEquals(object1, object1);
                assertNotEquals(object1, object2);
            }

            @Test
            void to_string() {
                Object object = beanClass.newInstance();
                assertEquals("Proxy[IClazz]{}", object.toString());
            }

            @Test
            void hash_code() {
                Object object = beanClass.newInstance();
                assertEquals(System.identityHashCode(object), object.hashCode());
            }
        }
    }

    private BeanClass<Object> createBeanClass(String name) {
        return BeanClassProxy.create(typeOf(name));
    }

    @SuppressWarnings("unchecked")
    private Class<Object> typeOf(String expression) {
        return (Class<Object>) executor().main().returnExpression(expression + ".class").evaluate();
    }

    @SuppressWarnings("unchecked")
    private Object valueOf(String expression) {
        return executor().main().returnExpression(expression).evaluate();
    }

    private void givenClass(String code) {
        executor().addClass(code);
    }
}

//TODO property setter
//TODO getGenericType
//TODO annotation
