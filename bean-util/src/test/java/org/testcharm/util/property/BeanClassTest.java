package org.testcharm.util.property;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.util.*;

import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.testcharm.util.JavaExecutor.executor;

class BeanClassTest {

    @BeforeEach
    void reset() {
        executor().main().importDependency("org.testcharm.util.BeanClass");
    }

    @Nested
    class CommonPojo {

        @Nested
        class CacheInstance {

            @Test
            void should_cache_instance_by_type() {
                givenClass(String.join("\n",
                        "public class Clazz {}"
                ));
                Class<Object> type = typeOf("Clazz");
                assertTrue(BeanClass.create(type) == BeanClass.create(type));
            }
        }

        @Nested
        class PropertyAndAccessor {

            @Nested
            class FieldProperty {

                @Nested
                class ByModifiers {

                    @Nested
                    class Public {

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
                        void included_in_type_properties() {
                            Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                            assertThat(properties.keySet()).containsExactly("field");

                            Property<?> property = properties.get("field");
                            assertEquals("field", property.getName());
                            assertEquals(beanClass, property.getBeanType());
                            assertEquals(int.class, property.getWriterType().getType());
                            assertEquals(int.class, property.getReaderType().getType());
                        }

                        @Test
                        void get_set_value_by_property() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            assertEquals(100, (int) property.getValue(object));

                            property.setValue(object, 1000);
                            assertEquals(1000, (int) property.getValue(object));
                        }

                        @Test
                        void get_set_value_directly() {
                            Object object = beanClass.newInstance();

                            assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));

                            beanClass.setPropertyValue(object, "field", 1000);

                            assertEquals(1000, (int) beanClass.getPropertyValue(object, "field"));
                        }
                    }

                    @Nested
                    class PublicStatic {

                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    public static int field = 100;",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void not_included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).isEmpty();
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void support_get_set_value_directly() {
                            Object object = beanClass.newInstance();

                            assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));

                            beanClass.setPropertyValue(object, "field", 1000);

                            assertEquals(1000, (int) beanClass.getPropertyValue(object, "field"));
                        }
                    }

                    @Nested
                    class PublicFinal {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    public final int field = 100;",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void included_reader_in_type_properties() {
                            Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                            assertThat(properties.keySet()).containsExactly("field");

                            Property<?> property = properties.get("field");
                            assertEquals("field", property.getName());
                            assertEquals(beanClass, property.getBeanType());
                            assertThrows(NoSuchAccessorException.class, () -> property.getWriterType());
                            assertEquals(int.class, property.getReaderType().getType());
                        }

                        @Test
                        void get_value_by_property_and_can_not_set_value() {
                            Object object = beanClass.newInstance();

                            Property<Object> property = beanClass.getProperty("field");

                            assertEquals(100, (int) property.getValue(object));

                            assertThrows(NoSuchAccessorException.class, () -> property.setValue(object, 1000));
                        }

                        @Test
                        void get_value_directly_and_can_not_set_value() {
                            Object object = beanClass.newInstance();

                            assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));
                            assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 1000));
                        }
                    }

                    @Nested
                    class PublicStaticFinal {

                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    public static final int field = 100;",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void not_included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).isEmpty();
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void get_value_directly_and_can_not_set_value() {
                            Object object = beanClass.newInstance();

                            assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));
                            assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 1000));
                        }
                    }

                    @Nested
                    class Private {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private int field = 100;",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void not_included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).isEmpty();
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void can_not_get_set_value_directly() {
                            Object object = beanClass.newInstance();

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                            assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 1));
                        }
                    }

                    @Nested
                    class Protected {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    protected int field = 100;",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void not_included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).isEmpty();
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void can_not_get_set_value_directly() {
                            Object object = beanClass.newInstance();

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                            assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 1));
                        }
                    }

                    @Nested
                    class PackagePrivate {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    int field = 100;",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void not_included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).isEmpty();
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void can_not_get_set_value_directly() {
                            Object object = beanClass.newInstance();

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                            assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 1));
                        }
                    }
                }

                @Nested
                class Inherited {

                    @Nested
                    class InBaseClass {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Base {",
                                    "    public int field = 100;",
                                    "}"
                            ));
                            givenClass(String.join("\n",
                                    "public class Clazz extends Base{",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void included_in_type_properties() {
                            Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                            assertThat(properties.keySet()).containsExactly("field");

                            Property<?> property = properties.get("field");
                            assertEquals("field", property.getName());
                            assertEquals(beanClass, property.getBeanType());
                            assertEquals(int.class, property.getWriterType().getType());
                            assertEquals(int.class, property.getReaderType().getType());
                        }

                        @Test
                        void get_set_value_by_property() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            assertEquals(100, (int) property.getValue(object));

                            property.setValue(object, 1000);
                            assertEquals(1000, (int) property.getValue(object));
                        }

                        @Test
                        void get_set_value_directly() {
                            Object object = beanClass.newInstance();

                            assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));

                            beanClass.setPropertyValue(object, "field", 1000);

                            assertEquals(1000, (int) beanClass.getPropertyValue(object, "field"));
                        }

                    }

                    @Nested
                    class InBaseAndSubClass {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Base {",
                                    "    public int field = 200;",
                                    "}"
                            ));
                            givenClass(String.join("\n",
                                    "public class Clazz extends Base{",
                                    "    public long field = 100;",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void include_sub_class_field_in_type_properties() {
                            Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                            assertThat(properties.keySet()).containsExactly("field");

                            Property<?> property = properties.get("field");
                            assertEquals("field", property.getName());
                            assertEquals(beanClass, property.getBeanType());
                            assertEquals(long.class, property.getWriterType().getType());
                            assertEquals(long.class, property.getReaderType().getType());
                        }

                        @Test
                        void get_set_value_by_property() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            assertEquals(100L, (long) property.getValue(object));

                            property.setValue(object, 1000L);

                            assertEquals(1000L, (long) property.getValue(object));
                        }

                        @Test
                        void get_set_value_directly() {
                            Object object = beanClass.newInstance();

                            assertEquals(100L, (long) beanClass.getPropertyValue(object, "field"));

                            beanClass.setPropertyValue(object, "field", 1000L);

                            assertEquals(1000L, (long) beanClass.getPropertyValue(object, "field"));
                        }

                        @Test
                        void get_set_base_class_value() {
                            BeanClass<? super Object> baseBeanClass = BeanClass.create(beanClass.getType().getSuperclass());
                            Object object = beanClass.newInstance();

                            assertEquals(100L, (long) beanClass.getPropertyValue(object, "field"));
                            assertEquals(200, (int) baseBeanClass.getPropertyValue(object, "field"));

                            beanClass.setPropertyValue(object, "field", 1000L);
                            baseBeanClass.setPropertyValue(object, "field", 2000);

                            assertEquals(1000L, (long) beanClass.getPropertyValue(object, "field"));
                            assertEquals(2000, (int) baseBeanClass.getPropertyValue(object, "field"));
                        }
                    }

                    @Nested
                    class StaticInBaseClass {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Base {",
                                    "    public static int field = 100;",
                                    "}"
                            ));
                            givenClass(String.join("\n",
                                    "public class Clazz extends Base{",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void not_included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).isEmpty();
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void support_get_set_value_directly() {
                            Object object = beanClass.newInstance();

                            assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));

                            beanClass.setPropertyValue(object, "field", 1000);

                            assertEquals(1000, (int) beanClass.getPropertyValue(object, "field"));
                        }
                    }

                    @Nested
                    class StaticInBaseAndSubClass {

                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Base {",
                                    "    public static int field = 200;",
                                    "}"
                            ));
                            givenClass(String.join("\n",
                                    "public class Clazz extends Base{",
                                    "    public static long field = 100;",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void not_included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).isEmpty();
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void get_set_value_directly() {
                            Object object = beanClass.newInstance();

                            assertEquals(100L, (long) beanClass.getPropertyValue(object, "field"));

                            beanClass.setPropertyValue(object, "field", 1000L);

                            assertEquals(1000L, (long) beanClass.getPropertyValue(object, "field"));
                        }

                        @Test
                        void get_set_base_class_value() {
                            BeanClass<? super Object> baseBeanClass = BeanClass.create(beanClass.getType().getSuperclass());
                            Object object = beanClass.newInstance();

                            assertEquals(100L, (long) beanClass.getPropertyValue(object, "field"));
                            assertEquals(200, (int) baseBeanClass.getPropertyValue(object, "field"));

                            beanClass.setPropertyValue(object, "field", 1000L);
                            baseBeanClass.setPropertyValue(object, "field", 2000);

                            assertEquals(1000L, (long) beanClass.getPropertyValue(object, "field"));
                            assertEquals(2000, (int) baseBeanClass.getPropertyValue(object, "field"));
                        }
                    }
                }

                @Nested
                class GenericType {

                    @Nested
                    class SimpleGenericType {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    public java.util.List<String> field= new java.util.ArrayList();",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @SneakyThrows
                        @Test
                        void return_generic_type_as_property_reader_type() {
                            BeanClass<?> fieldType = beanClass.getPropertyReader("field").getType();

                            assertThat(fieldType).isInstanceOf(GenericBeanClass.class);

                            assertThat(fieldType.getTypeArguments(0).map(BeanClass::getType)).hasValue(Sneaky.cast(String.class));
                        }

                        @SuppressWarnings("unchecked")
                        @Test
                        void get_and_set_value() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            assertThat((Object) property.getValue(object)).isInstanceOf(java.util.List.class);
                            assertThat((java.util.List<?>) property.getValue(object)).isEmpty();

                            property.setValue(object, asList("hello", "world"));

                            assertThat((java.util.List<String>) property.getValue(object)).containsExactly("hello", "world");
                        }
                    }

                    @Nested
                    class NestedGenericType {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    public java.util.List<java.util.List<String>> field= new java.util.ArrayList();",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @SneakyThrows
                        @Test
                        void return_nested_generic_type_as_property_reader_type() {
                            BeanClass<?> fieldType = beanClass.getPropertyReader("field").getType();

                            assertThat(fieldType).isInstanceOf(GenericBeanClass.class);

                            BeanClass<?> elementType = fieldType.getTypeArguments(0).get();
                            assertThat(elementType).isInstanceOf(GenericBeanClass.class);
                            assertThat(elementType.getType()).isEqualTo(Sneaky.cast(java.util.List.class));
                            assertThat(elementType.getTypeArguments(0).map(BeanClass::getType)).hasValue(Sneaky.cast(String.class));
                        }

                        @SuppressWarnings("unchecked")
                        @Test
                        void get_and_set_value() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            assertThat((Object) property.getValue(object)).isInstanceOf(java.util.List.class);
                            assertThat((java.util.List<?>) property.getValue(object)).isEmpty();

                            property.setValue(object, asList(asList("hello", "world")));

                            assertThat((java.util.List<java.util.List<String>>) property.getValue(object))
                                    .containsExactly(asList("hello", "world"));
                        }
                    }

                    @Nested
                    class RawGenericType {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    public java.util.List field= new java.util.ArrayList();",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @SneakyThrows
                        @Test
                        void return_raw_type_as_property_reader_type() {
                            BeanClass<?> fieldType = beanClass.getPropertyReader("field").getType();

                            assertThat(fieldType).isNotInstanceOf(GenericBeanClass.class);
                            assertThat(fieldType.getType()).isEqualTo(Sneaky.cast(java.util.List.class));
                            assertThat(fieldType.getTypeArguments(0)).isEmpty();
                            assertThat(fieldType.getElementType()).isEqualTo(BeanClass.create(Object.class));
                        }

                        @SuppressWarnings("unchecked")
                        @Test
                        void get_and_set_value() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            assertThat((Object) property.getValue(object)).isInstanceOf(java.util.List.class);
                            assertThat((java.util.List<?>) property.getValue(object)).isEmpty();

                            property.setValue(object, asList("hello", "world"));

                            assertThat((java.util.List<Object>) property.getValue(object)).containsExactly("hello", "world");
                        }

                    }
                }

                @Nested
                class AutoConvert {
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
                    void auto_convert_to_correct_type() {
                        Object object = beanClass.newInstance();

                        beanClass.setPropertyValue(object, "field", "1000");

                        assertEquals(1000, beanClass.getPropertyValue(object, "field"));
                    }
                }
            }

            @Nested
            class GetterProperty {

                @Nested
                class ByModifiers {

                    @Nested
                    class Public {

                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private int field = 100;",
                                    "    public int getField() { return field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void included_in_type_properties() {
                            Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                            assertThat(properties.keySet()).containsExactly("field");

                            Property<?> property = properties.get("field");
                            assertEquals("field", property.getName());
                            assertEquals(beanClass, property.getBeanType());
                            assertEquals(int.class, property.getReaderType().getType());
                        }

                        @Test
                        void get_value_by_property_and_can_not_set_value() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            assertEquals(100, (int) property.getValue(object));

                            assertThrows(NoSuchAccessorException.class, () -> property.setValue(object, 1000));
                        }

                        @Test
                        void get_value_directly_and_can_not_set_value() {
                            Object object = beanClass.newInstance();

                            assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));
                            assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 1000));
                        }
                    }

                    @Nested
                    class PublicStatic {

                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private static int field = 100;",
                                    "    public static int getField() { return field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void not_included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).isEmpty();
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void get_value_directly_and_can_not_set_value() {
                            Object object = beanClass.newInstance();

                            assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));
                            assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 1000));
                        }
                    }

                    @Nested
                    class Private {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private int field = 100;",
                                    "    private int getField() { return field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void not_included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).isEmpty();
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void can_not_get_set_value_directly() {
                            Object object = beanClass.newInstance();

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                            assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 1000));
                        }
                    }

                    @Nested
                    class Protected {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private int field = 100;",
                                    "    protected int getField() { return field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void not_included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).isEmpty();
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void can_not_get_set_value_directly() {
                            Object object = beanClass.newInstance();

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                            assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 1000));
                        }
                    }

                    @Nested
                    class PackagePrivate {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private int field = 100;",
                                    "    int getField() { return field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void not_included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).isEmpty();
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void can_not_get_set_value_directly() {
                            Object object = beanClass.newInstance();

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                            assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 1000));
                        }
                    }
                }

                @Nested
                class Inherited {

                    @Nested
                    class InBaseClass {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Base {",
                                    "    private int field = 100;",
                                    "    public int getField() { return field; }",
                                    "}"
                            ));
                            givenClass(String.join("\n",
                                    "public class Clazz extends Base{",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void included_in_type_properties() {
                            Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                            assertThat(properties.keySet()).containsExactly("field");

                            Property<?> property = properties.get("field");
                            assertEquals("field", property.getName());
                            assertEquals(beanClass, property.getBeanType());
                            assertEquals(int.class, property.getReaderType().getType());
                        }

                        @Test
                        void get_value_by_property_and_can_not_set_value() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            assertEquals(100, (int) property.getValue(object));

                            assertThrows(NoSuchAccessorException.class, () -> property.setValue(object, 1000));
                        }

                        @Test
                        void get_value_directly_and_can_not_set_value() {
                            Object object = beanClass.newInstance();

                            assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));
                            assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 1000));
                        }
                    }

                    @Nested
                    class InBaseAndSubClass {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Base {",
                                    "    public int getField() { return 200; }",
                                    "}"
                            ));
                            givenClass(String.join("\n",
                                    "public class Clazz extends Base{",
                                    "    @Override",
                                    "    public int getField() { return 100; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void included_in_type_properties() {
                            Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                            assertThat(properties.keySet()).containsExactly("field");

                            Property<?> property = properties.get("field");
                            assertEquals("field", property.getName());
                            assertEquals(beanClass, property.getBeanType());
                            assertEquals(int.class, property.getReaderType().getType());
                        }

                        @Test
                        void get_value_by_property_and_can_not_set_value() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            assertEquals(100, (int) property.getValue(object));

                            assertThrows(NoSuchAccessorException.class, () -> property.setValue(object, 1000));
                        }

                        @Test
                        void get_value_directly_and_can_not_set_value() {
                            Object object = beanClass.newInstance();

                            assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));
                            assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 1000));
                        }

                        @Test
                        void get_value_always_use_sub_class_getter() {
                            BeanClass<? super Object> baseBeanClass = BeanClass.create(beanClass.getType().getSuperclass());
                            Object object = beanClass.newInstance();

                            assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));
                            assertEquals(100, (int) baseBeanClass.getPropertyValue(object, "field"));

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 0));
                            assertThrows(NoSuchAccessorException.class, () -> baseBeanClass.setPropertyValue(object, "field", 0));
                        }
                    }

                    @Nested
                    class StaticInBaseClass {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Base {",
                                    "    private static int field = 100;",
                                    "    public static int getField() { return field; }",
                                    "}"
                            ));
                            givenClass(String.join("\n",
                                    "public class Clazz extends Base{",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void not_included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).isEmpty();
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void support_get_value_directly() {
                            Object object = beanClass.newInstance();

                            assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));
                        }
                    }

                    @Nested
                    class StaticInBaseAndSubClass {

                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Base {",
                                    "    private static int field = 200;",
                                    "    public static int getField() { return field; }",
                                    "}"
                            ));
                            givenClass(String.join("\n",
                                    "public class Clazz extends Base{",
                                    "    private static int field = 100;",
                                    "    public static int getField() { return field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void not_included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).isEmpty();
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void get_value_directly() {
                            Object object = beanClass.newInstance();

                            assertEquals(100, beanClass.getPropertyValue(object, "field"));
                        }

                        @Test
                        void get_base_class_value() {
                            BeanClass<? super Object> baseBeanClass = BeanClass.create(beanClass.getType().getSuperclass());
                            Object object = beanClass.newInstance();

                            assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));
                            assertEquals(200, (int) baseBeanClass.getPropertyValue(object, "field"));
                        }
                    }
                }

                @Nested
                class GenericType {

                    @Nested
                    class SimpleGenericType {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private java.util.List<String> field = new java.util.ArrayList();",
                                    "    public java.util.List<String> getField() { return field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @SneakyThrows
                        @Test
                        void return_generic_type_as_property_reader_type() {
                            BeanClass<?> fieldType = beanClass.getPropertyReader("field").getType();

                            assertThat(fieldType).isInstanceOf(GenericBeanClass.class);

                            assertThat(fieldType.getTypeArguments(0).map(BeanClass::getType)).hasValue(Sneaky.cast(String.class));
                        }

                        @SuppressWarnings("unchecked")
                        @Test
                        void get_value_and_can_not_set() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            assertThat((Object) property.getValue(object)).isInstanceOf(java.util.List.class);
                            assertThat((java.util.List<String>) property.getValue(object)).isEmpty();

                            assertThrows(NoSuchAccessorException.class, () -> property.setValue(object, asList("hello", "world")));
                        }
                    }

                    @Nested
                    class NestedGenericType {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private java.util.List<java.util.List<String>> field = new java.util.ArrayList();",
                                    "    public java.util.List<java.util.List<String>> getField() { return field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @SneakyThrows
                        @Test
                        void return_nested_generic_type_as_property_reader_type() {
                            BeanClass<?> fieldType = beanClass.getPropertyReader("field").getType();

                            assertThat(fieldType).isInstanceOf(GenericBeanClass.class);

                            BeanClass<?> elementType = fieldType.getTypeArguments(0).get();
                            assertThat(elementType).isInstanceOf(GenericBeanClass.class);
                            assertThat(elementType.getType()).isEqualTo(Sneaky.cast(java.util.List.class));
                            assertThat(elementType.getTypeArguments(0).map(BeanClass::getType)).hasValue(Sneaky.cast(String.class));
                        }

                        @SuppressWarnings("unchecked")
                        @Test
                        void get_value_and_can_not_set() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            assertThat((Object) property.getValue(object)).isInstanceOf(java.util.List.class);
                            assertThat((java.util.List<java.util.List<String>>) property.getValue(object)).isEmpty();

                            assertThrows(NoSuchAccessorException.class, () -> property.setValue(object, asList(asList("hello", "world"))));
                        }
                    }

                    @Nested
                    class RawGenericType {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private java.util.List field = new java.util.ArrayList();",
                                    "    public java.util.List getField() { return field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @SneakyThrows
                        @Test
                        void return_raw_type_as_property_reader_type() {
                            BeanClass<?> fieldType = beanClass.getPropertyReader("field").getType();

                            assertThat(fieldType).isNotInstanceOf(GenericBeanClass.class);
                            assertThat(fieldType.getType()).isEqualTo(Sneaky.cast(java.util.List.class));
                            assertThat(fieldType.getTypeArguments(0)).isEmpty();
                            assertThat(fieldType.getElementType()).isEqualTo(BeanClass.create(Object.class));
                        }

                        @SuppressWarnings("unchecked")
                        @Test
                        void get_value_and_can_not_set() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            assertThat((Object) property.getValue(object)).isInstanceOf(java.util.List.class);
                            assertThat((java.util.List<Object>) property.getValue(object)).isEmpty();

                            assertThrows(NoSuchAccessorException.class, () -> property.setValue(object, asList("hello", "world")));
                        }
                    }

                }

                @Nested
                class DifferentFieldName {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    private int value = 100;",
                                "    public int getField() { return value; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void included_in_type_properties() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();
                        assertThat(properties.keySet()).containsExactly("field");

                        Property<?> property = properties.get("field");
                        assertEquals("field", property.getName());
                        assertEquals(beanClass, property.getBeanType());
                        assertEquals(int.class, property.getReaderType().getType());
                    }

                    @Test
                    void get_value_by_property_and_can_not_set_value() {
                        Object object = beanClass.newInstance();
                        Property<Object> property = beanClass.getProperty("field");

                        assertEquals(100, (int) property.getValue(object));

                        assertThrows(NoSuchAccessorException.class, () -> property.setValue(object, 1000));
                    }
                }
            }

            @Nested
            class SetterProperty {

                @Nested
                class ByModifiers {

                    @Nested
                    class Public {

                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private int field = 100;",
                                    "    public int getValue() { return field; }",
                                    "    public void setField(int field) { this.field = field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void included_in_type_properties() {
                            Map<String, ? extends Property<?>> properties = beanClass.getProperties();
                            assertThat(properties.keySet()).containsExactlyInAnyOrder("field", "value");

                            Property<?> fieldProperty = properties.get("field");
                            assertEquals("field", fieldProperty.getName());
                            assertEquals(beanClass, fieldProperty.getBeanType());
                            assertEquals(int.class, fieldProperty.getWriterType().getType());
                        }

                        @Test
                        void set_value_by_property_and_can_not_get_value() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            property.setValue(object, 1000);

                            assertEquals(1000, (int) beanClass.getPropertyValue(object, "value"));
                            assertThrows(NoSuchAccessorException.class, () -> property.getValue(object));
                        }

                        @Test
                        void set_value_directly_and_can_not_get_value() {
                            Object object = beanClass.newInstance();

                            beanClass.setPropertyValue(object, "field", 1000);

                            assertEquals(1000, (int) beanClass.getPropertyValue(object, "value"));
                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                        }
                    }

                    @Nested
                    class PublicStatic {

                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private static int field = 100;",
                                    "    public static int getValue() { return field; }",
                                    "    public static void setField(int field) { Clazz.field = field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void not_included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).isEmpty();
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void set_value_directly_and_can_not_get_value() {
                            Object object = beanClass.newInstance();

                            beanClass.setPropertyValue(object, "field", 1000);
                            assertEquals(1000, (int) beanClass.getPropertyValue(object, "value"));

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                        }
                    }

                    @Nested
                    class Private {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private int field = 100;",
                                    "    public int getValue() { return field; }",
                                    "    private void setField(int field) { this.field = field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).containsExactly("value");
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void can_not_set_value_directly_and_can_not_get_value() {
                            Object object = beanClass.newInstance();

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 1000));
                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                        }
                    }

                    @Nested
                    class Protected {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private int field = 100;",
                                    "    public int getValue() { return field; }",
                                    "    protected void setField(int field) { this.field = field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).containsExactly("value");
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void can_not_set_value_directly_and_can_not_get_value() {
                            Object object = beanClass.newInstance();

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 1000));
                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                        }
                    }

                    @Nested
                    class PackagePrivate {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private int field = 100;",
                                    "    public int getValue() { return field; }",
                                    "    void setField(int field) { this.field = field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).containsExactly("value");
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void can_not_set_value_directly_and_can_not_get_value() {
                            Object object = beanClass.newInstance();

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 1000));
                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                        }
                    }
                }

                @Nested
                class Inherited {

                    @Nested
                    class InBaseClass {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Base {",
                                    "    private int field = 100;",
                                    "    public int getValue() { return field; }",
                                    "    public void setField(int field) { this.field = field; }",
                                    "}"
                            ));
                            givenClass(String.join("\n",
                                    "public class Clazz extends Base{",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void included_in_type_properties() {
                            Map<String, ? extends Property<?>> properties = beanClass.getProperties();
                            assertThat(properties.keySet()).containsExactlyInAnyOrder("field", "value");

                            Property<?> fieldProperty = properties.get("field");
                            assertEquals("field", fieldProperty.getName());
                            assertEquals(beanClass, fieldProperty.getBeanType());
                            assertEquals(int.class, fieldProperty.getWriterType().getType());
                        }

                        @Test
                        void set_value_by_property_and_can_not_get_value() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            property.setValue(object, 1000);
                            assertEquals(1000, (int) beanClass.getPropertyValue(object, "value"));

                            assertThrows(NoSuchAccessorException.class, () -> property.getValue(object));
                        }

                        @Test
                        void set_value_directly_and_can_not_get_value() {
                            Object object = beanClass.newInstance();

                            beanClass.setPropertyValue(object, "field", 1000);
                            assertEquals(1000, (int) beanClass.getPropertyValue(object, "value"));

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                        }
                    }

                    @Nested
                    class InBaseAndSubClass {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Base {",
                                    "    private int field = 200;",
                                    "    public int getValue() { return field; }",
                                    "    public void setField(int field) { this.field = field; }",
                                    "}"
                            ));
                            givenClass(String.join("\n",
                                    "public class Clazz extends Base{",
                                    "    private int field = 100;",
                                    "    @Override",
                                    "    public int getValue() { return field; }",
                                    "    @Override",
                                    "    public void setField(int field) { this.field = field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void included_in_type_properties() {
                            Map<String, ? extends Property<?>> properties = beanClass.getProperties();
                            assertThat(properties.keySet()).containsExactlyInAnyOrder("field", "value");

                            Property<?> fieldProperty = properties.get("field");
                            assertEquals("field", fieldProperty.getName());
                            assertEquals(beanClass, fieldProperty.getBeanType());
                            assertEquals(int.class, fieldProperty.getWriterType().getType());
                        }

                        @Test
                        void set_value_by_property_and_can_not_get_value() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            property.setValue(object, 1000);
                            assertEquals(1000, (int) beanClass.getPropertyValue(object, "value"));

                            assertThrows(NoSuchAccessorException.class, () -> property.getValue(object));
                        }

                        @Test
                        void set_value_directly_and_can_not_get_value() {
                            Object object = beanClass.newInstance();

                            beanClass.setPropertyValue(object, "field", 1000);
                            assertEquals(1000, (int) beanClass.getPropertyValue(object, "value"));

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                        }

                        @Test
                        void set_value_always_use_sub_class_setter() {
                            BeanClass<? super Object> baseBeanClass = BeanClass.create(beanClass.getType().getSuperclass());
                            Object object = beanClass.newInstance();

                            beanClass.setPropertyValue(object, "field", 1000);
                            assertEquals(1000, (int) baseBeanClass.getPropertyValue(object, "value"));
                            assertEquals(1000, (int) beanClass.getPropertyValue(object, "value"));

                            baseBeanClass.setPropertyValue(object, "field", 2000);
                            assertEquals(2000, (int) baseBeanClass.getPropertyValue(object, "value"));
                            assertEquals(2000, (int) beanClass.getPropertyValue(object, "value"));

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                            assertThrows(NoSuchAccessorException.class, () -> baseBeanClass.getPropertyValue(object, "field"));
                        }
                    }

                    @Nested
                    class StaticInBaseClass {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Base {",
                                    "    public static int fieldValue = 100;",
                                    "    public static void setField(int i) { fieldValue=i; }",
                                    "}"
                            ));
                            givenClass(String.join("\n",
                                    "public class Clazz extends Base{",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void not_included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).isEmpty();
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void support_get_value_directly() {
                            Object object = beanClass.newInstance();

                            beanClass.setPropertyValue(object, "field", 100);
                            assertEquals(100, beanClass.getPropertyValue(object, "fieldValue"));
                        }
                    }

                    @Nested
                    class StaticInBaseAndSubClass {

                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Base {",
                                    "    public static int fieldValue;",
                                    "    public static void setField(int i) { fieldValue=i; }",
                                    "}"
                            ));
                            givenClass(String.join("\n",
                                    "public class Clazz extends Base{",
                                    "    public static int fieldValue;",
                                    "    public static void setField(int i) { fieldValue=i; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @Test
                        void not_included_in_type_properties() {
                            assertThat(beanClass.getProperties().keySet()).isEmpty();
                        }

                        @Test
                        void can_not_find_property_by_name() {
                            assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                        }

                        @Test
                        void set_value_directly() {
                            Object object = beanClass.newInstance();

                            beanClass.setPropertyValue(object, "field", 100);

                            assertEquals(100, beanClass.getPropertyValue(object, "fieldValue"));
                        }

                        @Test
                        void set_base_class_value() {
                            BeanClass<? super Object> baseBeanClass = BeanClass.create(beanClass.getType().getSuperclass());
                            Object object = beanClass.newInstance();

                            beanClass.setPropertyValue(object, "field", 100);
                            baseBeanClass.setPropertyValue(object, "field", 200);

                            assertEquals(100, (int) beanClass.getPropertyValue(object, "fieldValue"));
                            assertEquals(200, (int) baseBeanClass.getPropertyValue(object, "fieldValue"));
                        }
                    }
                }

                @Nested
                class GenericType {

                    @Nested
                    class SimpleGenericType {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private java.util.List<String> field = new java.util.ArrayList();",
                                    "    public java.util.List<String> getValue() { return field; }",
                                    "    public void setField(java.util.List<String> field) { this.field = field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @SneakyThrows
                        @Test
                        void return_generic_type_as_property_writer_type() {
                            BeanClass<?> fieldType = beanClass.getPropertyWriter("field").getType();

                            assertThat(fieldType).isInstanceOf(GenericBeanClass.class);

                            assertThat(fieldType.getTypeArguments(0).map(BeanClass::getType)).hasValue(Sneaky.cast(String.class));
                        }

                        @SuppressWarnings("unchecked")
                        @Test
                        void set_value_by_property_and_can_not_get_value() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            assertThrows(NoSuchAccessorException.class, () -> property.getValue(object));
                            property.setValue(object, asList("hello", "world"));

                            assertThat((java.util.List<String>) beanClass.getPropertyValue(object, "value")).containsExactly("hello", "world");
                        }

                        @SuppressWarnings("unchecked")
                        @Test
                        void set_value_directly_and_can_not_get_value() {
                            Object object = beanClass.newInstance();

                            beanClass.setPropertyValue(object, "field", asList("hello", "world"));
                            assertThat((java.util.List<String>) beanClass.getPropertyValue(object, "value")).containsExactly("hello", "world");

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                        }
                    }

                    @Nested
                    class NestedGenericType {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private java.util.List<java.util.List<String>> field = new java.util.ArrayList();",
                                    "    public java.util.List<java.util.List<String>> getValue() { return field; }",
                                    "    public void setField(java.util.List<java.util.List<String>> field) { this.field = field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @SneakyThrows
                        @Test
                        void return_nested_generic_type_as_property_writer_type() {
                            BeanClass<?> fieldType = beanClass.getPropertyWriter("field").getType();

                            assertThat(fieldType).isInstanceOf(GenericBeanClass.class);

                            BeanClass<?> elementType = fieldType.getTypeArguments(0).get();
                            assertThat(elementType).isInstanceOf(GenericBeanClass.class);
                            assertThat(elementType.getType()).isEqualTo(Sneaky.cast(java.util.List.class));
                            assertThat(elementType.getTypeArguments(0).map(BeanClass::getType)).hasValue(Sneaky.cast(String.class));
                        }

                        @SuppressWarnings("unchecked")
                        @Test
                        void set_value_by_property_and_can_not_get_value() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            property.setValue(object, asList(asList("hello", "world")));
                            assertThat((java.util.List<java.util.List<String>>) beanClass.getPropertyValue(object, "value"))
                                    .containsExactly(asList("hello", "world"));

                            assertThrows(NoSuchAccessorException.class, () -> property.getValue(object));
                        }

                        @SuppressWarnings("unchecked")
                        @Test
                        void set_value_directly_and_can_not_get_value() {
                            Object object = beanClass.newInstance();

                            beanClass.setPropertyValue(object, "field", asList(asList("hello", "world")));
                            assertThat((java.util.List<java.util.List<String>>) beanClass.getPropertyValue(object, "value"))
                                    .containsExactly(asList("hello", "world"));

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                        }
                    }

                    @Nested
                    class RawGenericType {
                        private BeanClass<Object> beanClass;

                        @BeforeEach
                        void prepareClass() {
                            givenClass(String.join("\n",
                                    "public class Clazz {",
                                    "    private java.util.List field = new java.util.ArrayList();",
                                    "    public java.util.List getValue() { return field; }",
                                    "    public void setField(java.util.List field) { this.field = field; }",
                                    "}"
                            ));
                            beanClass = createBeanClass("Clazz");
                        }

                        @SneakyThrows
                        @Test
                        void return_raw_type_as_property_writer_type() {
                            BeanClass<?> fieldType = beanClass.getPropertyWriter("field").getType();

                            assertThat(fieldType).isNotInstanceOf(GenericBeanClass.class);
                            assertThat(fieldType.getType()).isEqualTo(Sneaky.cast(java.util.List.class));
                            assertThat(fieldType.getTypeArguments(0)).isEmpty();
                            assertThat(fieldType.getElementType()).isEqualTo(BeanClass.create(Object.class));
                        }

                        @SuppressWarnings("unchecked")
                        @Test
                        void set_value_by_property_and_can_not_get_value() {
                            Object object = beanClass.newInstance();
                            Property<Object> property = beanClass.getProperty("field");

                            property.setValue(object, asList("hello", "world"));
                            assertThat((java.util.List<Object>) beanClass.getPropertyValue(object, "value")).containsExactly("hello", "world");

                            assertThrows(NoSuchAccessorException.class, () -> property.getValue(object));
                        }

                        @SuppressWarnings("unchecked")
                        @Test
                        void set_value_directly_and_can_not_get_value() {
                            Object object = beanClass.newInstance();

                            beanClass.setPropertyValue(object, "field", asList("hello", "world"));
                            assertThat((java.util.List<Object>) beanClass.getPropertyValue(object, "value")).containsExactly("hello", "world");

                            assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                        }
                    }
                }

                @Nested
                class DifferentFieldName {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    private int value = 100;",
                                "    public void setField(int f) { value = f; }",
                                "    public int getValue() { return value; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void included_in_type_properties() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();
                        assertThat(properties.keySet()).containsExactlyInAnyOrder("field", "value");

                        Property<?> property = properties.get("field");
                        assertEquals("field", property.getName());
                        assertEquals(beanClass, property.getBeanType());
                    }

                    @Test
                    void set_value_by_property_and_can_not_get_value() {
                        Object object = beanClass.newInstance();
                        Property<Object> property = beanClass.getProperty("field");


                        property.setValue(object, 1000);
                        assertEquals(1000, (int) beanClass.getPropertyValue(object, "value"));

                        assertThrows(NoSuchAccessorException.class, () -> property.getValue(object));
                    }
                }

                @Nested
                class AutoConvert {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    private int field = 100;",
                                "    public void setField(int i) { this.field = i; }",
                                "    public int getField() { return field; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void auto_convert_to_correct_type() {
                        Object object = beanClass.newInstance();

                        beanClass.setPropertyValue(object, "field", "1000");

                        assertEquals(1000, beanClass.getPropertyValue(object, "field"));
                    }
                }
            }

            @Nested
            class MixedAccessorAndField {

                @Nested
                class PublicAccessorAndField {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    public int value = 0;",
                                "    public int valueForGetterSetter = 100;",

                                "    public int getValue() { return valueForGetterSetter; }",
                                "    public void setValue(int i) { valueForGetterSetter = i; }",

                                "    public int getValueField() { return value; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void accessor_over_field() {
                        Object object = beanClass.newInstance();

                        assertThat(beanClass.getPropertyValue(object, "value")).isEqualTo(100);

                        beanClass.setPropertyValue(object, "value", 1000);
                        assertThat(beanClass.getPropertyValue(object, "value")).isEqualTo(1000);
                        assertThat(beanClass.getPropertyValue(object, "valueForGetterSetter")).isEqualTo(1000);

                        assertThat(beanClass.getPropertyValue(object, "valueField")).isEqualTo(0);
                    }
                }

                @Nested
                class PublicGetterAndField {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    public int value = 0;",
                                "    public int valueForGetterSetter = 100;",
                                "    public int getValue() { return valueForGetterSetter; }",
                                "    public int getValueField() { return value; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void getter_over_field() {
                        Object object = beanClass.newInstance();

                        assertThat(beanClass.getPropertyValue(object, "value")).isEqualTo(100);
                    }

                    @Test
                    void set_value_should_use_field() {
                        Object object = beanClass.newInstance();

                        beanClass.setPropertyValue(object, "value", 200);

                        assertThat(beanClass.getPropertyValue(object, "valueForGetterSetter")).isEqualTo(100);
                        assertThat(beanClass.getPropertyValue(object, "valueField")).isEqualTo(200);
                    }
                }

                @Nested
                class PublicSetterAndField {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    public int value = 0;",
                                "    public int valueForGetterSetter = 100;",
                                "    public void setValue(int i) { valueForGetterSetter = i; }",
                                "    public int getValueField() { return value; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void setter_over_field() {
                        Object object = beanClass.newInstance();

                        beanClass.setPropertyValue(object, "value", 200);

                        assertThat(beanClass.getPropertyValue(object, "valueForGetterSetter")).isEqualTo(200);
                        assertThat(beanClass.getPropertyValue(object, "valueField")).isEqualTo(0);
                    }

                    @Test
                    void get_value_should_use_field() {
                        Object object = beanClass.newInstance();

                        assertThat(beanClass.getPropertyValue(object, "value")).isEqualTo(0);
                    }
                }
            }

            @Nested
            class ValidityAccessor {

                @Nested
                class JavaGetClass {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void not_included_get_class() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                        assertThat(properties.keySet()).isEmpty();
                    }

                    @Test
                    void can_not_find_property_by_name() {
                        assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("class"));
                    }

                    @Test
                    void get_class_directly() {
                        Object object = beanClass.newInstance();

                        assertEquals(beanClass.getType(), beanClass.getPropertyValue(object, "class"));
                    }
                }

                @Nested
                class SetterWithReturn {

                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    private int field;",
                                "    public int getField() { return field; }",
                                "    public Clazz setField(int i) { this.field = i; return this; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void included_in_type_properties() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                        assertThat(properties.keySet()).containsExactly("field");

                        Property<?> property = properties.get("field");
                        assertEquals("field", property.getName());
                        assertEquals(beanClass, property.getBeanType());
                        assertEquals(int.class, property.getWriterType().getType());
                    }

                    @Test
                    void set_value() {
                        Object object = beanClass.newInstance();
                        Property<Object> property = beanClass.getProperty("field");

                        property.setValue(object, 1000);
                        assertEquals(1000, (int) property.getValue(object));

                        beanClass.setPropertyValue(object, "field", 2000);
                        assertEquals(2000, (int) beanClass.getPropertyValue(object, "field"));
                    }
                }

                @Nested
                class PrimitiveBoolean {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    private boolean valid = true;",
                                "    public boolean isValid() { return valid; }",
                                "    public void setValid(boolean f) { this.valid=f; }",
                                "    private boolean inValid = false;",
                                "    public boolean getInValid() { return inValid; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void start_with_is_should_be_included_in_type_properties() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                        assertThat(properties.keySet()).containsExactly("valid");

                        Property<?> property = properties.get("valid");
                        assertEquals("valid", property.getName());
                        assertEquals(beanClass, property.getBeanType());
                        assertEquals(boolean.class, property.getReaderType().getType());
                        assertEquals(boolean.class, property.getWriterType().getType());
                    }

                    @Test
                    void get_set_value() {
                        Object object = beanClass.newInstance();
                        Property<Object> property = beanClass.getProperty("valid");

                        assertEquals(true, property.getValue(object));
                        assertEquals(true, beanClass.getPropertyValue(object, "valid"));

                        property.setValue(object, false);
                        assertEquals(false, beanClass.getPropertyValue(object, "valid"));

                    }

                    @Test
                    void get_prefix_method_is_not_valid_getter_for_primitive_boolean() {
                        assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(beanClass.newInstance(), "inValid"));
                        assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("inValid"));
                    }
                }

                @Nested
                class BoxedBoolean {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    private Boolean valid = true;",
                                "    public Boolean getValid() { return valid; }",
                                "    public void setValid(Boolean f) { this.valid=f; }",
                                "    private Boolean inValid = false;",
                                "    public Boolean isInValid() { return inValid; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void start_with_is_should_be_included_in_type_properties() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                        assertThat(properties.keySet()).containsExactly("valid");

                        Property<?> property = properties.get("valid");
                        assertEquals("valid", property.getName());
                        assertEquals(beanClass, property.getBeanType());
                        assertEquals(Boolean.class, property.getReaderType().getType());
                        assertEquals(Boolean.class, property.getWriterType().getType());
                    }

                    @Test
                    void get_set_value() {
                        Object object = beanClass.newInstance();
                        Property<Object> property = beanClass.getProperty("valid");

                        assertEquals(true, property.getValue(object));
                        assertEquals(true, beanClass.getPropertyValue(object, "valid"));

                        property.setValue(object, false);
                        assertEquals(false, beanClass.getPropertyValue(object, "valid"));

                    }

                    @Test
                    void is_prefix_method_is_not_valid_getter_for_boxed_boolean() {
                        assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(beanClass.newInstance(), "inValid"));
                        assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("inValid"));
                    }
                }

                @Nested
                class MoreThanOneUpperCaseLetter {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    private int i;",
                                "    public int getINT() { return i; }",
                                "    public void setINT(int i) { this.i = i; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void should_keep_the_original_name() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                        assertThat(properties.keySet()).containsExactlyInAnyOrder("INT");

                        Property<?> property = properties.get("INT");
                        assertEquals("INT", property.getName());
                        assertEquals(beanClass, property.getBeanType());
                        assertEquals(int.class, property.getReaderType().getType());
                        assertEquals(int.class, property.getWriterType().getType());
                    }

                    @SneakyThrows
                    @Test
                    void get_set_value() {
                        Object object = beanClass.newInstance();
                        Property<Object> property = beanClass.getProperty("INT");

                        property.setValue(object, 1);
                        assertEquals(1, (int) property.getValue(object));

                        beanClass.setPropertyValue(object, "INT", 2);
                        assertEquals(2, beanClass.getPropertyValue(object, "INT"));
                    }
                }

                @Nested
                class OneUpperCaseLetter {

                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    private int i;",
                                "    public int getI() { return i; }",
                                "    public void setI(int i) { this.i = i; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void should_keep_the_original_name() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                        assertThat(properties.keySet()).containsExactlyInAnyOrder("i");

                        Property<?> property = properties.get("i");
                        assertEquals("i", property.getName());
                        assertEquals(beanClass, property.getBeanType());
                        assertEquals(int.class, property.getReaderType().getType());
                        assertEquals(int.class, property.getWriterType().getType());
                    }

                    @SneakyThrows
                    @Test
                    void get_set_value() {
                        Object object = beanClass.newInstance();
                        Property<Object> property = beanClass.getProperty("i");

                        property.setValue(object, 1);
                        assertEquals(1, (int) property.getValue(object));

                        beanClass.setPropertyValue(object, "i", 2);
                        assertEquals(2, beanClass.getPropertyValue(object, "i"));
                    }
                }

                @Nested
                class OneLowerCaseLetter {

                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    private int i;",
                                "    public int geti() { return i; }",
                                "    public void seti(int i) { this.i = i; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void should_keep_the_original_name() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                        assertThat(properties.keySet()).containsExactlyInAnyOrder("i");

                        Property<?> property = properties.get("i");
                        assertEquals("i", property.getName());
                        assertEquals(beanClass, property.getBeanType());
                        assertEquals(int.class, property.getReaderType().getType());
                        assertEquals(int.class, property.getWriterType().getType());
                    }

                    @SneakyThrows
                    @Test
                    void get_set_value() {
                        Object object = beanClass.newInstance();
                        Property<Object> property = beanClass.getProperty("i");

                        property.setValue(object, 1);
                        assertEquals(1, (int) property.getValue(object));

                        beanClass.setPropertyValue(object, "i", 2);
                        assertEquals(2, beanClass.getPropertyValue(object, "i"));
                    }
                }

                @Nested
                class FirstLetterLowerCase {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    private int i;",
                                "    public int getiValue() { return i; }",
                                "    public void setiValue(int i) { this.i = i; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void should_keep_the_original_name() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                        assertThat(properties.keySet()).containsExactlyInAnyOrder("iValue");

                        Property<?> property = properties.get("iValue");
                        assertEquals("iValue", property.getName());
                        assertEquals(beanClass, property.getBeanType());
                        assertEquals(int.class, property.getReaderType().getType());
                        assertEquals(int.class, property.getWriterType().getType());
                    }

                    @SneakyThrows
                    @Test
                    void get_set_value() {
                        Object object = beanClass.newInstance();
                        Property<Object> property = beanClass.getProperty("iValue");

                        property.setValue(object, 1);
                        assertEquals(1, (int) property.getValue(object));

                        beanClass.setPropertyValue(object, "iValue", 2);
                        assertEquals(2, beanClass.getPropertyValue(object, "iValue"));
                    }
                }

                @Nested
                class EmptyName {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    public int get() { return 0; }",
                                "    public void set(int i) { }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void not_included_in_type_properties() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                        assertThat(properties.keySet()).isEmpty();
                    }

                    @Test
                    void can_not_find_property_by_name() {
                        assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty(""));
                    }

                    @Test
                    void cannot_get_set_value() {
                        Object object = beanClass.newInstance();

                        assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, ""));
                        assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "", 0));
                    }
                }

                @Nested
                class NonAscii {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    private int i;",
                                "    public int get整数() { return i; }",
                                "    public void set整数(int i) { this.i = i; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void should_keep_the_original_name() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                        assertThat(properties.keySet()).containsExactlyInAnyOrder("整数");

                        Property<?> property = properties.get("整数");
                        assertEquals("整数", property.getName());
                        assertEquals(beanClass, property.getBeanType());
                        assertEquals(int.class, property.getReaderType().getType());
                        assertEquals(int.class, property.getWriterType().getType());
                    }

                    @SneakyThrows
                    @Test
                    void get_set_value() {
                        Object object = beanClass.newInstance();
                        Property<Object> property = beanClass.getProperty("整数");

                        property.setValue(object, 1);
                        assertEquals(1, (int) property.getValue(object));

                        beanClass.setPropertyValue(object, "整数", 2);
                        assertEquals(2, beanClass.getPropertyValue(object, "整数"));
                    }
                }

                @Nested
                class GetWithArg {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    public int getValue(int i) { return 0; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void not_included_in_type_properties() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                        assertThat(properties.keySet()).isEmpty();
                    }

                    @Test
                    void can_not_find_property_by_name() {
                        assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("value"));
                    }

                    @Test
                    void cannot_get_value() {
                        Object object = beanClass.newInstance();

                        assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "value"));
                    }
                }

                @Nested
                class SetMoreThanOneArg {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    public void setValue(int i, int j) { }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void not_included_in_type_properties() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                        assertThat(properties.keySet()).isEmpty();
                    }

                    @Test
                    void can_not_find_property_by_name() {
                        assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("value"));
                    }

                    @Test
                    void cannot_set_value() {
                        Object object = beanClass.newInstance();

                        assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "value", 0));
                    }
                }

                @Nested
                class SetOverloaded {

                    @Test
                    void included_in_type_properties_but_depends_on_class_get_methods() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    private Object value;",
                                "    public void setValue(String s) {this.value=s;}",
                                "    public void setValue(int i) {this.value=i;}",
                                "}"
                        ));

                        BeanClass<Object> beanClass = createBeanClass("Clazz");

                        assertNotNull(beanClass.getProperties().get("value"));
                    }
                }

                @Nested
                class GetReturnVoid {

                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    public void getValue() {}",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void not_included_in_type_properties() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                        assertThat(properties.keySet()).isEmpty();
                    }

                    @Test
                    void can_not_find_property_by_name() {
                        assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("value"));
                    }

                    @Test
                    void cannot_get_value() {
                        Object object = beanClass.newInstance();

                        assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "value"));
                    }
                }

                @Nested
                class InvalidGetterAndValidSetter {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    public int i;",
                                "    public int getValue(int i) { return 0; }",
                                "    public void setValue(int i) { this.i=i; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void included_in_type_properties() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                        assertThat(properties.keySet()).containsExactlyInAnyOrder("i", "value");

                        Property<?> property = properties.get("value");
                        assertEquals("value", property.getName());
                        assertEquals(beanClass, property.getBeanType());
                        assertEquals(int.class, property.getWriterType().getType());

                        assertThrows(NoSuchAccessorException.class, property::getReaderType);
                    }

                    @Test
                    void set_value_but_cannot_get_value() {
                        Object object = beanClass.newInstance();
                        Property<Object> property = beanClass.getProperty("value");

                        property.setValue(object, 100);
                        assertEquals(100, (int) beanClass.getPropertyValue(object, "i"));

                        assertThrows(NoSuchAccessorException.class, () -> property.getValue(object));

                        beanClass.setPropertyValue(object, "value", 200);
                        assertEquals(200, (int) beanClass.getPropertyValue(object, "i"));

                        assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "value"));
                    }
                }

                @Nested
                class ValidGetterAndInvalidSetter {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    public int i=100;",
                                "    public int getValue() { return i; }",
                                "    public void setValue(int i, int j) { this.i=i; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void included_in_type_properties() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                        assertThat(properties.keySet()).containsExactlyInAnyOrder("i", "value");

                        Property<?> property = properties.get("value");
                        assertEquals("value", property.getName());
                        assertEquals(beanClass, property.getBeanType());
                        assertEquals(int.class, property.getReaderType().getType());

                        assertThrows(NoSuchAccessorException.class, property::getWriterType);
                    }

                    @Test
                    void get_value_but_cannot_set_value() {
                        Object object = beanClass.newInstance();
                        Property<Object> property = beanClass.getProperty("value");

                        assertEquals(100, (int) property.getValue(object));
                        assertThrows(NoSuchAccessorException.class, property::getWriterType);

                        assertEquals(100, (int) beanClass.getPropertyValue(object, "value"));
                        assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "value", 0));
                    }
                }

                @Nested
                class DifferentGetterSetterType {
                    private BeanClass<Object> beanClass;

                    @BeforeEach
                    void prepareClass() {
                        givenClass(String.join("\n",
                                "public class Clazz {",
                                "    public int i=100;",
                                "    public String getValue() { return String.valueOf(i); }",
                                "    public void setValue(int i) { this.i=i; }",
                                "}"
                        ));
                        beanClass = createBeanClass("Clazz");
                    }

                    @Test
                    void included_in_type_properties_with_different_data_type() {
                        Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                        assertThat(properties.keySet()).containsExactlyInAnyOrder("i", "value");

                        Property<?> property = properties.get("value");
                        assertEquals("value", property.getName());
                        assertEquals(beanClass, property.getBeanType());
                        assertEquals(String.class, property.getReaderType().getType());
                        assertEquals(int.class, property.getWriterType().getType());
                    }

                    @Test
                    void get_set_value() {
                        Object object = beanClass.newInstance();
                        Property<Object> property = beanClass.getProperty("value");

                        assertEquals("100", property.getValue(object));
                        property.setValue(object, 1000);
                        assertEquals("1000", property.getValue(object));

                        beanClass.setPropertyValue(object, "value", 2000);
                        assertEquals("2000", beanClass.getPropertyValue(object, "value"));
                    }
                }
            }
        }
    }

    @Nested
    class NonStaticInner {

        @Nested
        class CacheInstance {

            @Test
            void should_cache_instance_by_type() {
                givenClass(String.join("\n",
                        "public class Clazz {",
                        "    public Inner inner = new Inner();",
                        "    public class Inner {",
                        "        public int field = 100;",
                        "    }",
                        "}"
                ));
                Class<Object> type = typeOf("Clazz.Inner");
                assertTrue(BeanClass.create(type) == BeanClass.create(type));
            }
        }

        @Nested
        class PropertyAndAccessor {

            @Nested
            class FieldProperty {

                private BeanClass<Object> beanClass;
                private BeanClass<Object> declaringBeanClass;

                @SuppressWarnings("unchecked")
                @BeforeEach
                void prepareClass() {
                    givenClass(String.join("\n",
                            "public class Clazz {",
                            "    public Inner inner = new Inner();",
                            "    public class Inner {",
                            "        public int field = 100;",
                            "    }",
                            "}"
                    ));
                    beanClass = createBeanClass("Clazz.Inner");
                    declaringBeanClass = (BeanClass<Object>) BeanClass.create(beanClass.getType().getDeclaringClass());
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                    assertEquals(int.class, property.getWriterType().getType());
                    assertEquals(int.class, property.getReaderType().getType());
                }

                @Test
                void get_set_value_by_property() {
                    Object object = declaringBeanClass.getPropertyValue(declaringBeanClass.newInstance(), "inner");
                    Property<Object> property = beanClass.getProperty("field");

                    assertEquals(100, (int) property.getValue(object));

                    property.setValue(object, 1000);
                    assertEquals(1000, (int) property.getValue(object));
                }

                @Test
                void get_set_value_directly() {
                    Object object = declaringBeanClass.getPropertyValue(declaringBeanClass.newInstance(), "inner");

                    assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));

                    beanClass.setPropertyValue(object, "field", 1000);

                    assertEquals(1000, (int) beanClass.getPropertyValue(object, "field"));
                }
            }

            @Nested
            class AccessorProperty {

                private BeanClass<Object> beanClass;
                private BeanClass<Object> declaringBeanClass;

                @SuppressWarnings("unchecked")
                @BeforeEach
                void prepareClass() {
                    givenClass(String.join("\n",
                            "public class Clazz {",
                            "    public Inner inner = new Inner();",
                            "    public class Inner {",
                            "        private int field = 100;",
                            "        public int getField() { return field; }",
                            "        public void setField(int f) { this.field = f; }",
                            "    }",
                            "}"
                    ));
                    beanClass = createBeanClass("Clazz.Inner");
                    declaringBeanClass = (BeanClass<Object>) BeanClass.create(beanClass.getType().getDeclaringClass());
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                    assertEquals(int.class, property.getWriterType().getType());
                    assertEquals(int.class, property.getReaderType().getType());
                }

                @Test
                void get_set_value_by_property() {
                    Object object = declaringBeanClass.getPropertyValue(declaringBeanClass.newInstance(), "inner");
                    Property<Object> property = beanClass.getProperty("field");

                    assertEquals(100, (int) property.getValue(object));

                    property.setValue(object, 1000);
                    assertEquals(1000, (int) property.getValue(object));
                }

                @Test
                void get_set_value_directly() {
                    Object object = declaringBeanClass.getPropertyValue(declaringBeanClass.newInstance(), "inner");

                    assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));

                    beanClass.setPropertyValue(object, "field", 1000);

                    assertEquals(1000, (int) beanClass.getPropertyValue(object, "field"));
                }
            }
        }
    }

    @Nested
    class AnonymousClass {

        @Nested
        class CacheInstance {

            @Test
            void should_cache_instance_by_type() {
                givenClass(String.join("\n",
                        "public class Clazz {",
                        "    public int field = 100;",
                        "}"
                ));
                Object instance = valueOf("new Clazz() { }");
                assertTrue(BeanClass.createFrom(instance) != BeanClass.createFrom(instance));
            }
        }

        @Nested
        class PropertyAndAccessor {

            @Nested
            class FieldInConcreteClass {
                private Object object;
                private BeanClass<Object> beanClass;
                private BeanClass<Object> baseBeanClass;

                @BeforeEach
                void prepareClass() {
                    givenClass(String.join("\n",
                            "public class Clazz {",
                            "    public int field = 100;",
                            "}"
                    ));
                    object = valueOf("new Clazz() { }");
                    beanClass = BeanClass.createFrom(object);
                    baseBeanClass = BeanClass.create(beanClass.getType().getSuperclass());
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                    assertEquals(int.class, property.getWriterType().getType());
                    assertEquals(int.class, property.getReaderType().getType());
                }

                @Test
                void get_set_value_by_property() {
                    Property<Object> property = beanClass.getProperty("field");

                    assertEquals(100, (int) property.getValue(object));

                    property.setValue(object, 1000);
                    assertEquals(1000, (int) property.getValue(object));
                }

                @Test
                void get_set_value_directly() {
                    assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));

                    beanClass.setPropertyValue(object, "field", 1000);

                    assertEquals(1000, (int) beanClass.getPropertyValue(object, "field"));
                }

                @Test
                void get_set_value_via_super_bean_class() {
                    assertEquals(100, (int) baseBeanClass.getPropertyValue(object, "field"));

                    baseBeanClass.setPropertyValue(object, "field", 1000);

                    assertEquals(1000, (int) baseBeanClass.getPropertyValue(object, "field"));
                }
            }

            @Nested
            class FieldInAbstractClass {
                private Object object;
                private BeanClass<Object> beanClass;
                private BeanClass<Object> baseBeanClass;

                @BeforeEach
                void prepareClass() {
                    givenClass(String.join("\n",
                            "public abstract class AbstractClazz {",
                            "    public int field = 100;",
                            "}"
                    ));
                    object = valueOf("new AbstractClazz() { }");
                    beanClass = BeanClass.createFrom(object);
                    baseBeanClass = BeanClass.create(beanClass.getType().getSuperclass());
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                    assertEquals(int.class, property.getWriterType().getType());
                    assertEquals(int.class, property.getReaderType().getType());
                }

                @Test
                void get_set_value_by_property() {
                    Property<Object> property = beanClass.getProperty("field");

                    assertEquals(100, (int) property.getValue(object));

                    property.setValue(object, 1000);
                    assertEquals(1000, (int) property.getValue(object));
                }

                @Test
                void get_set_value_directly() {
                    assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));

                    beanClass.setPropertyValue(object, "field", 1000);

                    assertEquals(1000, (int) beanClass.getPropertyValue(object, "field"));
                }

                @Test
                void get_set_value_via_super_bean_class() {
                    assertEquals(100, (int) baseBeanClass.getPropertyValue(object, "field"));

                    baseBeanClass.setPropertyValue(object, "field", 1000);

                    assertEquals(1000, (int) baseBeanClass.getPropertyValue(object, "field"));
                }
            }

            @Nested
            class FieldInAnonymousClass {
                private Object object;
                private BeanClass<Object> beanClass;

                @BeforeEach
                void prepareClass() {
                    givenClass(String.join("\n",
                            "public class AbstractClazz {}"
                    ));
                    object = valueOf("new AbstractClazz() { public int field = 100;}");
                    beanClass = BeanClass.createFrom(object);
                }

                @Test
                void included_in_type_properties() {
                    assertThat(beanClass.getProperties().keySet()).isEmpty();
                }

                @SneakyThrows
                @Test
                void can_not_find_property_by_name() {
                    assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                }

                @Test
                void can_not_get_set_value() {
                    assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                    assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 0));
                }
            }

            @Nested
            class PropertyInConcreteClass {
                private Object object;
                private BeanClass<Object> beanClass;
                private BeanClass<Object> baseBeanClass;

                @BeforeEach
                void prepareClass() {
                    givenClass(String.join("\n",
                            "public class Clazz {",
                            "    private int field = 100;",
                            "    public int getField() { return field; }",
                            "    public void setField(int f) { this.field = f; }",
                            "}"
                    ));
                    object = valueOf("new Clazz() { }");
                    beanClass = BeanClass.createFrom(object);
                    baseBeanClass = BeanClass.create(beanClass.getType().getSuperclass());
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                    assertEquals(int.class, property.getWriterType().getType());
                    assertEquals(int.class, property.getReaderType().getType());
                }

                @Test
                void get_set_value_by_property() {
                    Property<Object> property = beanClass.getProperty("field");

                    assertEquals(100, (int) property.getValue(object));

                    property.setValue(object, 1000);
                    assertEquals(1000, (int) property.getValue(object));
                }

                @Test
                void get_set_value_directly() {
                    assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));

                    beanClass.setPropertyValue(object, "field", 1000);

                    assertEquals(1000, (int) beanClass.getPropertyValue(object, "field"));
                }

                @Test
                void get_set_value_via_super_bean_class() {
                    assertEquals(100, (int) baseBeanClass.getPropertyValue(object, "field"));

                    baseBeanClass.setPropertyValue(object, "field", 1000);

                    assertEquals(1000, (int) baseBeanClass.getPropertyValue(object, "field"));
                }
            }

            @Nested
            class PropertyInAbstractClass {
                private Object object;
                private BeanClass<Object> beanClass;
                private BeanClass<Object> baseBeanClass;

                @BeforeEach
                void prepareClass() {
                    givenClass(String.join("\n",
                            "public abstract class Clazz {",
                            "    private int field = 100;",
                            "    public int getField() { return field; }",
                            "    public void setField(int f) { this.field = f; }",
                            "}"
                    ));
                    object = valueOf("new Clazz() { }");
                    beanClass = BeanClass.createFrom(object);
                    baseBeanClass = BeanClass.create(beanClass.getType().getSuperclass());
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                    assertEquals(int.class, property.getWriterType().getType());
                    assertEquals(int.class, property.getReaderType().getType());
                }

                @Test
                void get_set_value_by_property() {
                    Property<Object> property = beanClass.getProperty("field");

                    assertEquals(100, (int) property.getValue(object));

                    property.setValue(object, 1000);
                    assertEquals(1000, (int) property.getValue(object));
                }

                @Test
                void get_set_value_directly() {
                    assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));

                    beanClass.setPropertyValue(object, "field", 1000);

                    assertEquals(1000, (int) beanClass.getPropertyValue(object, "field"));
                }

                @Test
                void get_set_value_via_super_bean_class() {
                    assertEquals(100, (int) baseBeanClass.getPropertyValue(object, "field"));

                    baseBeanClass.setPropertyValue(object, "field", 1000);

                    assertEquals(1000, (int) baseBeanClass.getPropertyValue(object, "field"));
                }
            }

            @Nested
            class PropertyInInterface {
                private Object object;
                private BeanClass<Object> beanClass;
                private BeanClass<Object> baseBeanClass;

                @BeforeEach
                void prepareClass() {
                    givenClass(String.join("\n",
                            "public interface IClazz {",
                            "    default int getField() { return 100; }",
                            "}"
                    ));
                    object = valueOf("new IClazz() { }");
                    beanClass = BeanClass.createFrom(object);
                    baseBeanClass = (BeanClass<Object>) BeanClass.create(beanClass.getType().getInterfaces()[0]);
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                    assertEquals(int.class, property.getReaderType().getType());
                }

                @Test
                void get_set_value_by_property() {
                    Property<Object> property = beanClass.getProperty("field");

                    assertEquals(100, (int) property.getValue(object));
                }

                @Test
                void get_set_value_directly() {
                    assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));
                }

                @Test
                void get_set_value_via_super_bean_class() {
                    assertEquals(100, (int) baseBeanClass.getPropertyValue(object, "field"));
                }
            }

            @Nested
            class PropertyInAnonymousClass {
                private Object object;
                private BeanClass<Object> beanClass;

                @BeforeEach
                void prepareClass() {
                    givenClass(String.join("\n",
                            "public class Clazz {}"
                    ));
                    object = valueOf(String.join("\n", "new Clazz() { ",
                            "    private int field = 100;",
                            "    public int getField() { return field; }",
                            "    public void setField(int f) { this.field = f; }",
                            "}"));
                    beanClass = BeanClass.createFrom(object);
                }

                @Test
                void included_in_type_properties() {
                    assertThat(beanClass.getProperties().keySet()).isEmpty();
                }

                @SneakyThrows
                @Test
                void can_not_find_property_by_name() {
                    assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                }

                @Test
                void can_not_get_set_value() {
                    assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                    assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 0));
                }
            }
        }
    }

    @Nested
    class LocalClass {

        @Nested
        class CacheInstance {

            @Test
            void should_cache_instance_by_type() {
                givenClass(String.join("\n",
                        "public class Clazz {",
                        "    public int field = 100;",
                        "    public static Object getLocalClassInstance() {",
                        "        class Local extends Clazz {};",
                        "        return new Local();",
                        "    }",
                        "}"
                ));
                Object instance = valueOf("Clazz.getLocalClassInstance()");
                assertTrue(BeanClass.createFrom(instance) != BeanClass.createFrom(instance));
            }
        }

        @Nested
        class PropertyAndAccessor {

            @Nested
            class FieldInSuperClass {
                private Object object;
                private BeanClass<Object> beanClass;
                private BeanClass<Object> baseBeanClass;

                @BeforeEach
                void prepareClass() {
                    givenClass(String.join("\n",
                            "public class Clazz {",
                            "    public int field = 100;",
                            "    public static Object getLocalClassInstance() {",
                            "        class Local extends Clazz {};",
                            "        return new Local();",
                            "    }",
                            "}"
                    ));

                    object = valueOf("Clazz.getLocalClassInstance()");
                    beanClass = BeanClass.createFrom(object);
                    baseBeanClass = BeanClass.create(beanClass.getType().getSuperclass());
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                    assertEquals(int.class, property.getWriterType().getType());
                    assertEquals(int.class, property.getReaderType().getType());
                }

                @Test
                void get_set_value_by_property() {
                    Property<Object> property = beanClass.getProperty("field");

                    assertEquals(100, (int) property.getValue(object));

                    property.setValue(object, 1000);
                    assertEquals(1000, (int) property.getValue(object));
                }

                @Test
                void get_set_value_directly() {
                    assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));

                    beanClass.setPropertyValue(object, "field", 1000);

                    assertEquals(1000, (int) beanClass.getPropertyValue(object, "field"));
                }

                @Test
                void get_set_value_via_super_bean_class() {
                    assertEquals(100, (int) baseBeanClass.getPropertyValue(object, "field"));

                    baseBeanClass.setPropertyValue(object, "field", 1000);

                    assertEquals(1000, (int) baseBeanClass.getPropertyValue(object, "field"));
                }
            }

            @Nested
            class PropertyInSuperClass {
                private Object object;
                private BeanClass<Object> beanClass;
                private BeanClass<Object> baseBeanClass;

                @BeforeEach
                void prepareClass() {
                    givenClass(String.join("\n",
                            "public class Clazz {",
                            "    private int field = 100;",
                            "    public int getField() { return field; }",
                            "    public void setField(int f) { this.field = f; }",
                            "    public static Object getLocalClassInstance() {",
                            "        class Local extends Clazz {};",
                            "        return new Local();",
                            "    }",
                            "}"
                    ));

                    object = valueOf("Clazz.getLocalClassInstance()");
                    beanClass = BeanClass.createFrom(object);
                    baseBeanClass = BeanClass.create(beanClass.getType().getSuperclass());
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                    assertEquals(int.class, property.getWriterType().getType());
                    assertEquals(int.class, property.getReaderType().getType());
                }

                @Test
                void get_set_value_by_property() {
                    Property<Object> property = beanClass.getProperty("field");

                    assertEquals(100, (int) property.getValue(object));

                    property.setValue(object, 1000);
                    assertEquals(1000, (int) property.getValue(object));
                }

                @Test
                void get_set_value_directly() {
                    assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));

                    beanClass.setPropertyValue(object, "field", 1000);

                    assertEquals(1000, (int) beanClass.getPropertyValue(object, "field"));
                }

                @Test
                void get_set_value_via_super_bean_class() {
                    assertEquals(100, (int) baseBeanClass.getPropertyValue(object, "field"));

                    baseBeanClass.setPropertyValue(object, "field", 1000);

                    assertEquals(1000, (int) baseBeanClass.getPropertyValue(object, "field"));
                }
            }

            @Nested
            class FieldInLocalClass {
                private Object object;
                private BeanClass<Object> beanClass;

                @BeforeEach
                void prepareClass() {
                    givenClass(String.join("\n",
                            "public class Clazz {",
                            "    public static Object getLocalClassInstance() {",
                            "        class Local {" +
                                    "    public int field = 100;",
                            "};",
                            "        return new Local();",
                            "    }",
                            "}"
                    ));
                    object = valueOf("Clazz.getLocalClassInstance()");
                    beanClass = BeanClass.createFrom(object);
                }

                @Test
                void included_in_type_properties() {
                    assertThat(beanClass.getProperties().keySet()).isEmpty();
                }

                @SneakyThrows
                @Test
                void can_not_find_property_by_name() {
                    assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                }

                @Test
                void can_not_get_set_value() {
                    assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                    assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 0));
                }
            }

            @Nested
            class PropertyInLocalClass {
                private Object object;
                private BeanClass<Object> beanClass;

                @BeforeEach
                void prepareClass() {
                    givenClass(String.join("\n",
                            "public class Clazz {",
                            "    public static Object getLocalClassInstance() {",
                            "        class Local {" +
                                    "    private int field = 100;",
                            "    public int getField() { return field; }",
                            "    public void setField(int f) { this.field = f; }",
                            "};",
                            "        return new Local();",
                            "    }",
                            "}"
                    ));
                    object = valueOf("Clazz.getLocalClassInstance()");
                    beanClass = BeanClass.createFrom(object);
                }

                @Test
                void included_in_type_properties() {
                    assertThat(beanClass.getProperties().keySet()).isEmpty();
                }

                @SneakyThrows
                @Test
                void can_not_find_property_by_name() {
                    assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                }

                @Test
                void can_not_get_set_value() {
                    assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                    assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 0));
                }
            }
        }
    }

    @Nested
    class Lambda {

        @Nested
        class CacheInstance {

            @Test
            void should_cache_instance_by_type() {
                givenClass(String.join("\n",
                        "public interface IClazz {",
                        "    default int getField() { return 100; }",
                        "    void run();",
                        "}"
                ));
                Object instance = valueOf("(IClazz)() -> {}");
                assertTrue(BeanClass.createFrom(instance) != BeanClass.createFrom(instance));
            }
        }

        @Nested
        class PropertyAndAccessor {

            @Nested
            class PropertyInInterface {
                private Object object;
                private BeanClass<Object> beanClass;
                private BeanClass<Object> baseBeanClass;

                @BeforeEach
                void prepareClass() {
                    givenClass(String.join("\n",
                            "public interface IClazz {",
                            "    default int getField() { return 100; }",
                            "    void run();",
                            "}"
                    ));
                    object = valueOf("(IClazz)() -> {}");
                    beanClass = BeanClass.createFrom(object);
                    baseBeanClass = (BeanClass<Object>) BeanClass.create(beanClass.getType().getInterfaces()[0]);
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                    assertEquals(int.class, property.getReaderType().getType());
                }

                @Test
                void get_set_value_by_property() {
                    Property<Object> property = beanClass.getProperty("field");

                    assertEquals(100, (int) property.getValue(object));
                }

                @Test
                void get_set_value_directly() {
                    assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));
                }

                @Test
                void get_set_value_via_super_bean_class() {
                    assertEquals(100, (int) baseBeanClass.getPropertyValue(object, "field"));
                }
            }
        }
    }

    private BeanClass<Object> createBeanClass(String name) {
        return BeanClass.create(typeOf(name));
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
