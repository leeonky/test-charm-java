package com.github.leeonky.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CollectionPropertyTest {

    public static class Bean {
        public String[] array;
        public String str;
        public Iterable<String> iterable;
        public Iterable<?> invalidIterable1;
        public Iterable invalidIterable2;
    }

    @Nested
    class ElementType {
        private final BeanClass<Bean> beanClass = BeanClass.create(Bean.class);

        @Test
        void get_element_or_property_type() {
            assertThat(beanClass.getPropertyReader("array").getElementOrPropertyType()).isEqualTo(String.class);
            assertThat(beanClass.getPropertyReader("str").getElementOrPropertyType()).isEqualTo(String.class);
        }

        @Nested
        class Array {

            @Test
            void get_element_type() {
                assertThat(beanClass.getPropertyReader("array").getElementType())
                        .isEqualTo(String.class);

                assertThat(beanClass.getPropertyReader("array").getElementType())
                        .isEqualTo(String.class);
            }
        }

        @Nested
        class Collections {

            @Test
            void get_element_type() {
                assertThat(beanClass.getPropertyReader("iterable").getElementType())
                        .isEqualTo(String.class);

                assertThat(beanClass.getPropertyReader("iterable").getElementType())
                        .isEqualTo(String.class);
            }

            @Test
            void should_raise_error_when_generic_type_params_not_specify() {
                assertThrows(IllegalArgumentException.class, () -> beanClass.getPropertyReader("invalidIterable1").getElementType());
                assertThrows(IllegalArgumentException.class, () -> beanClass.getPropertyReader("invalidIterable2").getElementType());
            }
        }
    }

    @Nested
    class CreateReadWrite {

        @Test
        void support_get_elements_return_null() {
            assertThat(BeanClass.getElements(null)).isEmpty();
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
                assertThat(BeanClass.getElements(new String[]{"hello", "world"}).get().collect(Collectors.toList()))
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
                assertThat(BeanClass.getElements(asList("hello", "world")).get().collect(Collectors.toList()))
                        .isEqualTo(asList("hello", "world"));
            }
        }
    }
}
