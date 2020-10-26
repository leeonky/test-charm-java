package com.github.leeonky.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.emptyList;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CollectionPropertyTest {

    public static class Bean {
        public String[] array;
        public String str;
        public Iterable<String> iterable;
        public Iterable<?> uncheckedIterable;
        public Iterable rawIterable;
    }

    @Nested
    class ElementType {
        private final BeanClass<Bean> beanClass = BeanClass.create(Bean.class);

        @Test
        void get_element_or_property_type() {
            assertThat(beanClass.getPropertyReader("array").getType().getElementOrPropertyType().getType()).isEqualTo(String.class);
            assertThat(beanClass.getPropertyReader("str").getType().getElementOrPropertyType().getType()).isEqualTo(String.class);
        }

        @Nested
        class Array {

            @Test
            void get_element_type() {
                assertThat(beanClass.getPropertyReader("array").getType().getElementType().getType())
                        .isEqualTo(String.class);

                assertThat(beanClass.getPropertyReader("array").getType().getElementType().getType())
                        .isEqualTo(String.class);
            }
        }

        @Nested
        class Collections {

            @Test
            void get_element_type() {
                assertThat(beanClass.getPropertyReader("iterable").getType().getElementType().getType())
                        .isEqualTo(String.class);

                assertThat(beanClass.getPropertyReader("iterable").getType().getElementType().getType())
                        .isEqualTo(String.class);
            }

            @Test
            void should_return_object_class_when_generic_type_params_not_specify() {
                assertThat(beanClass.getPropertyReader("uncheckedIterable").getType().getElementType().getType()).isEqualTo(Object.class);
                assertThat(beanClass.getPropertyReader("rawIterable").getType().getElementType().getType()).isEqualTo(Object.class);
            }
        }
    }

    @Nested
    class CreateReadWrite {

        @Test
        void support_null_input() {
            assertThrows(IllegalArgumentException.class, () -> BeanClass.arrayCollectionToStream(null));
        }

        @Test
        void should_raise_error_when_type_is_not_collection() {
            assertThrows(IllegalStateException.class, () -> BeanClass.create(Integer.class).createCollection(emptyList()));
        }

        @Nested
        class Array {

            @Test
            void support_create_with_elements() {
                BeanClass<String[]> beanClass = BeanClass.create(String[].class);

                Object collection = beanClass.createCollection(asList("a", "b"));

                assertThat(collection).isEqualTo(new String[]{"a", "b"});
            }

            @Test
            void support_get_elements() {
                assertThat(BeanClass.arrayCollectionToStream(new String[]{"hello", "world"}).collect(Collectors.toList()))
                        .isEqualTo(asList("hello", "world"));
            }
        }

        @Nested
        class Collections {

            @Test
            void support_create_list_with_elements() {
                BeanClass<Iterable> beanClass = BeanClass.create(Iterable.class);

                Object collection = beanClass.createCollection(asList("a", "b"));

                assertThat(collection).isEqualTo(asList("a", "b"));
            }

            @Test
            void support_create_set_with_elements() {
                BeanClass<Set> beanClass = BeanClass.create(Set.class);

                Object collection = beanClass.createCollection(asList("a", "b"));

                assertThat(collection).isEqualTo(new LinkedHashSet<>(asList("a", "b")));
            }

            @Test
            void support_create_class_instance() {
                BeanClass<LinkedList> beanClass = BeanClass.create(LinkedList.class);

                Object collection = beanClass.createCollection(asList("a", "b"));

                assertThat(collection).isEqualTo(new LinkedList<>(asList("a", "b")));
            }

            @Test
            void support_get_elements() {
                assertThat(BeanClass.arrayCollectionToStream(asList("hello", "world")).collect(Collectors.toList()))
                        .isEqualTo(asList("hello", "world"));
            }
        }
    }

    @Nested
    class SupportElementReadWrite {

        @Nested
        class Read {

            @Test
            void get_property_type_in_array_or_collection() {
                BeanClass<Bean> beanClass = BeanClass.create(Bean.class);

                assertThat(beanClass.getPropertyReader("iterable").getType().getPropertyReader("0").getType().getType())
                        .isEqualTo(String.class);

                assertThat(beanClass.getPropertyReader("array").getType().getPropertyReader("0").getType().getType())
                        .isEqualTo(String.class);
            }

            @Test
            void property_readers_should_return_empty() {
                BeanClass<Bean> beanClass = BeanClass.create(Bean.class);

                assertThat(beanClass.getPropertyReader("iterable").getType().getPropertyReaders())
                        .isEmpty();

                assertThat(beanClass.getPropertyReader("array").getType().getPropertyReaders())
                        .isEmpty();
            }

            @Test
            void read_array_value_by_index() {
                int[] ints = new int[]{2, 3};
                BeanClass<int[]> beanClass = BeanClass.create(int[].class);

                assertThat(beanClass.getPropertyValue(ints, "0")).isEqualTo(2);

                BeanClass<List> listBeanClass = BeanClass.create(List.class);

                assertThat(listBeanClass.getPropertyValue(asList("", "hello"), "1")).isEqualTo("hello");
            }
        }

        @Nested
        class Write {

            @Test
            void get_property_type_in_array_or_collection() {
                BeanClass<Bean> beanClass = BeanClass.create(Bean.class);

                assertThat(beanClass.getPropertyReader("iterable").getType().getPropertyWriter("0").getType().getType())
                        .isEqualTo(String.class);

                assertThat(beanClass.getPropertyReader("array").getType().getPropertyWriter("0").getType().getType())
                        .isEqualTo(String.class);
            }

            @Test
            void property_readers_should_return_empty() {
                BeanClass<Bean> beanClass = BeanClass.create(Bean.class);

                assertThat(beanClass.getPropertyReader("iterable").getType().getPropertyWriters())
                        .isEmpty();

                assertThat(beanClass.getPropertyReader("array").getType().getPropertyWriters())
                        .isEmpty();
            }

            @Test
            void read_array_value_by_index() {
                int[] ints = new int[]{2, 3};
                BeanClass<int[]> beanClass = BeanClass.create(int[].class);

                beanClass.setPropertyValue(ints, "0", 0);

                assertThat(ints[0]).isEqualTo(0);

                BeanClass<List> listBeanClass = BeanClass.create(List.class);
                List<String> stringList = new ArrayList<>();
                stringList.add("");
                stringList.add("");
                listBeanClass.setPropertyValue(stringList, "0", "hello");

                assertThat(stringList).containsOnly("hello", "");
            }

            @Test
            void should_raise_error_when_collection_not_support_set() {
                assertThrows(IllegalArgumentException.class, () ->
                        BeanClass.create(Set.class).setPropertyValue(new HashSet(), "1", null));
            }
        }

        @Nested
        class _Property {

            @Test
            void get_collection_property() {
                BeanClass<?> type = BeanClass.create(Bean.class).getPropertyReader("iterable").getType();

                Property<?> property = type.getProperty("0");

                assertThat(property)
                        .hasFieldOrPropertyWithValue("name", "0")
                        .hasFieldOrPropertyWithValue("beanType", type);
            }
        }
    }
}
