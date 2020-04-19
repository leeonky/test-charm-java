package com.github.leeonky.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CollectionPropertyTest {
    private final BeanClass<Bean> beanClass = BeanClass.create(Bean.class);

    public static class Bean {
        public String[] array;
        public Iterable<String> iterable;
        public Iterable<?> invalidIterable1;
        public Iterable invalidIterable2;
    }

    @Nested
    class Array {

        @Nested
        class ElementType {

            @Test
            void get_element_type() {
                assertThat(beanClass.getPropertyReader("array").getElementPropertyType())
                        .isEqualTo(String.class);

                assertThat(beanClass.getPropertyReader("array").getElementPropertyType())
                        .isEqualTo(String.class);
            }
        }
    }

    @Nested
    class Collections {

        @Nested
        class ElementType {

            @Test
            void get_element_type() {
                assertThat(beanClass.getPropertyReader("iterable").getElementPropertyType())
                        .isEqualTo(String.class);

                assertThat(beanClass.getPropertyReader("iterable").getElementPropertyType())
                        .isEqualTo(String.class);
            }

            @Test
            void should_raise_error_when_generic_type_params_not_specify() {
                assertThrows(IllegalArgumentException.class, () -> beanClass.getPropertyReader("invalidIterable1").getElementPropertyType());
                assertThrows(IllegalArgumentException.class, () -> beanClass.getPropertyReader("invalidIterable2").getElementPropertyType());
            }
        }
    }
}
