package org.testcharm.util.property;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.util.*;

import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testcharm.util.JavaExecutor.executor;

class PojoTest {
    @BeforeEach
    void reset() {
        executor().main().importDependency("org.testcharm.util.BeanClass");
    }

    @Nested
    class GetTypeAndName {

        @Test
        void return_the_given_pojo_type() {
            givenClass(String.join("\n",
                    "package p;",
                    "public class Pojo {",
                    "    public int pubField;",
                    "    private int i;",
                    "    public int getPubField() { return i; }",
                    "    public void setPubField(int i) { this.pubField = pubField; }",
                    "}"
            ));

            Class<?> type = typeOf("p.Pojo");

            assertEquals(type, BeanClass.create(type).getType());
            assertEquals("Pojo", BeanClass.create(type).getSimpleName());
            assertEquals("p.Pojo", BeanClass.create(type).getName());
        }
    }

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
                            "public class Pojo {",
                            "    public int field = 100;",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                }

                @Test
                void find_property_by_name() {
                    Property<?> property = beanClass.getProperty("field");

                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
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
                            "public class Pojo {",
                            "    public static int field = 100;",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void not_in_type_properties() {
                    assertThat(beanClass.getProperties().keySet()).isEmpty();
                }

                @Test
                void can_not_find_property_by_name() {
                    assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                }

                void can_not_get_set_value_by_property() {
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
            class PublicFinal {
                private BeanClass<Object> beanClass;

                @BeforeEach
                void prepareClass() {
                    givenClass(String.join("\n",
                            "public class Pojo {",
                            "    public final int field = 100;",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                }

                @Test
                void find_property_by_name() {
                    Property<?> property = beanClass.getProperty("field");

                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
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
                            "public class Pojo {",
                            "    public static final int field = 100;",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void not_in_type_properties() {
                    assertThat(beanClass.getProperties().keySet()).isEmpty();
                }

                @Test
                void can_not_find_property_by_name() {
                    assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                }

                void can_not_get_set_value_by_property() {
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
                            "public class Pojo {",
                            "    private int field = 100;",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void not_in_type_properties() {
                    assertThat(beanClass.getProperties().keySet()).isEmpty();
                }

                @Test
                void can_not_find_property_by_name() {
                    assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                }

                void can_not_get_set_value_by_property() {
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
                            "public class Pojo {",
                            "    protected int field = 100;",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void not_in_type_properties() {
                    assertThat(beanClass.getProperties().keySet()).isEmpty();
                }

                @Test
                void can_not_find_property_by_name() {
                    assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                }

                void can_not_get_set_value_by_property() {
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
                            "public class Pojo {",
                            "    int field = 100;",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void not_in_type_properties() {
                    assertThat(beanClass.getProperties().keySet()).isEmpty();
                }

                @Test
                void can_not_find_property_by_name() {
                    assertThrows(NoSuchPropertyException.class, () -> beanClass.getProperty("field"));
                }

                void can_not_get_set_value_by_property() {
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
                            "public class Pojo extends Base{",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                }

                @Test
                void find_property_by_name() {
                    Property<?> property = beanClass.getProperty("field");

                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
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
                            "public class Pojo extends Base{",
                            "    public int field = 100;",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                }

                @Test
                void find_property_by_name() {
                    Property<?> property = beanClass.getProperty("field");

                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
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

                @Test
                void get_set_base_class_value() {
                    BeanClass<? super Object> baseBeanClass = BeanClass.create(beanClass.getType().getSuperclass());
                    Object object = beanClass.newInstance();

                    assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));
                    assertEquals(200, (int) baseBeanClass.getPropertyValue(object, "field"));

                    beanClass.setPropertyValue(object, "field", 1000);
                    baseBeanClass.setPropertyValue(object, "field", 2000);

                    assertEquals(1000, (int) beanClass.getPropertyValue(object, "field"));
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
                            "public class Pojo {",
                            "    public java.util.List<String> field= new java.util.ArrayList();",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
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
                            "public class Pojo {",
                            "    public java.util.List<java.util.List<String>> field= new java.util.ArrayList();",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
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
                            "public class Pojo {",
                            "    public java.util.List field= new java.util.ArrayList();",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
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
                            "public class Pojo {",
                            "    private int field = 100;",
                            "    public int getField() { return field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                }

                @Test
                void find_property_by_name() {
                    Property<?> property = beanClass.getProperty("field");

                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
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
                            "public class Pojo {",
                            "    private static int field = 100;",
                            "    public static int getField() { return field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void not_in_type_properties() {
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
                            "public class Pojo {",
                            "    private int field = 100;",
                            "    private int getField() { return field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void not_in_type_properties() {
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
                            "public class Pojo {",
                            "    private int field = 100;",
                            "    protected int getField() { return field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void not_in_type_properties() {
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
                            "public class Pojo {",
                            "    private int field = 100;",
                            "    int getField() { return field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void not_in_type_properties() {
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
                            "public class Pojo extends Base{",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                }

                @Test
                void find_property_by_name() {
                    Property<?> property = beanClass.getProperty("field");

                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
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
                            "public class Pojo extends Base{",
                            "    @Override",
                            "    public int getField() { return 100; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();

                    assertThat(properties.keySet()).containsExactly("field");

                    Property<?> property = properties.get("field");
                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
                }

                @Test
                void find_property_by_name() {
                    Property<?> property = beanClass.getProperty("field");

                    assertEquals("field", property.getName());
                    assertEquals(beanClass, property.getBeanType());
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
                void get_base_class_value() {
                    BeanClass<? super Object> baseBeanClass = BeanClass.create(beanClass.getType().getSuperclass());
                    Object object = beanClass.newInstance();

                    assertEquals(100, (int) beanClass.getPropertyValue(object, "field"));
                    assertEquals(100, (int) baseBeanClass.getPropertyValue(object, "field"));

                    assertThrows(NoSuchAccessorException.class, () -> beanClass.setPropertyValue(object, "field", 0));
                    assertThrows(NoSuchAccessorException.class, () -> baseBeanClass.setPropertyValue(object, "field", 0));
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
                            "public class Pojo {",
                            "    private java.util.List<String> field = new java.util.ArrayList();",
                            "    public java.util.List<String> getField() { return field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
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
                            "public class Pojo {",
                            "    private java.util.List<java.util.List<String>> field = new java.util.ArrayList();",
                            "    public java.util.List<java.util.List<String>> getField() { return field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
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
                            "public class Pojo {",
                            "    private java.util.List field = new java.util.ArrayList();",
                            "    public java.util.List getField() { return field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
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
                        "public class Pojo {",
                        "    private int value = 100;",
                        "    public int getField() { return value; }",
                        "}"
                ));
                beanClass = BeanClass.create(typeOf("Pojo"));
            }

            @Test
            void included_in_type_properties() {
                Map<String, ? extends Property<?>> properties = beanClass.getProperties();
                assertThat(properties.keySet()).containsExactly("field");

                Property<?> property = properties.get("field");
                assertEquals("field", property.getName());
                assertEquals(beanClass, property.getBeanType());
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
                            "public class Pojo {",
                            "    private int field = 100;",
                            "    public int getValue() { return field; }",
                            "    public void setField(int field) { this.field = field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();
                    assertThat(properties.keySet()).containsExactlyInAnyOrder("field", "value");

                    Property<?> fieldProperty = properties.get("field");
                    assertEquals("field", fieldProperty.getName());
                    assertEquals(beanClass, fieldProperty.getBeanType());
                }

                @Test
                void find_property_by_name() {
                    Property<?> property = beanClass.getProperty("field");

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
                            "public class Pojo {",
                            "    private static int field = 100;",
                            "    public static int getValue() { return field; }",
                            "    public static void setField(int field) { Pojo.field = field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void not_in_type_properties() {
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
                            "public class Pojo {",
                            "    private int field = 100;",
                            "    public int getValue() { return field; }",
                            "    private void setField(int field) { this.field = field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
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
                            "public class Pojo {",
                            "    private int field = 100;",
                            "    public int getValue() { return field; }",
                            "    protected void setField(int field) { this.field = field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
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
                            "public class Pojo {",
                            "    private int field = 100;",
                            "    public int getValue() { return field; }",
                            "    void setField(int field) { this.field = field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
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
                            "public class Pojo extends Base{",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();
                    assertThat(properties.keySet()).containsExactlyInAnyOrder("field", "value");

                    Property<?> fieldProperty = properties.get("field");
                    assertEquals("field", fieldProperty.getName());
                    assertEquals(beanClass, fieldProperty.getBeanType());
                }

                @Test
                void find_property_by_name() {
                    Property<?> property = beanClass.getProperty("field");

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

                @Test
                void set_value_directly_and_can_not_get_value() {
                    Object object = beanClass.newInstance();
                    Property<Object> property = beanClass.getProperty("field");

                    property.setValue(object, 1000);
                    assertEquals(1000, (int) beanClass.getPropertyValue(object, "value"));

                    assertThrows(NoSuchAccessorException.class, () -> property.getValue(object));
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
                            "public class Pojo extends Base{",
                            "    private int field = 100;",
                            "    @Override",
                            "    public int getValue() { return field; }",
                            "    @Override",
                            "    public void setField(int field) { this.field = field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
                }

                @Test
                void included_in_type_properties() {
                    Map<String, ? extends Property<?>> properties = beanClass.getProperties();
                    assertThat(properties.keySet()).containsExactlyInAnyOrder("field", "value");

                    Property<?> fieldProperty = properties.get("field");
                    assertEquals("field", fieldProperty.getName());
                    assertEquals(beanClass, fieldProperty.getBeanType());
                }

                @Test
                void find_property_by_name() {
                    Property<?> property = beanClass.getProperty("field");

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

                @Test
                void set_value_directly_and_can_not_get_value() {
                    Object object = beanClass.newInstance();

                    beanClass.setPropertyValue(object, "field", 1000);
                    assertEquals(1000, (int) beanClass.getPropertyValue(object, "value"));

                    assertThrows(NoSuchAccessorException.class, () -> beanClass.getPropertyValue(object, "field"));
                }

                @Test
                void set_base_class_value_and_can_not_get_value() {
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
        }

        @Nested
        class GenericType {

            @Nested
            class SimpleGenericType {
                private BeanClass<Object> beanClass;

                @BeforeEach
                void prepareClass() {
                    givenClass(String.join("\n",
                            "public class Pojo {",
                            "    private java.util.List<String> field = new java.util.ArrayList();",
                            "    public java.util.List<String> getValue() { return field; }",
                            "    public void setField(java.util.List<String> field) { this.field = field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
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
                            "public class Pojo {",
                            "    private java.util.List<java.util.List<String>> field = new java.util.ArrayList();",
                            "    public java.util.List<java.util.List<String>> getValue() { return field; }",
                            "    public void setField(java.util.List<java.util.List<String>> field) { this.field = field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
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
                            "public class Pojo {",
                            "    private java.util.List field = new java.util.ArrayList();",
                            "    public java.util.List getValue() { return field; }",
                            "    public void setField(java.util.List field) { this.field = field; }",
                            "}"
                    ));
                    beanClass = BeanClass.create(typeOf("Pojo"));
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
    }

    @SuppressWarnings("unchecked")
    private Class<Object> typeOf(String expression) {
        return (Class<Object>) executor().main().returnExpression(expression + ".class").evaluate();
    }

    private void givenClass(String code) {
        executor().addClass(code);
    }
}
