package com.github.leeonky.util;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyChainTest {

    @Nested
    class ToChain {

        @Test
        void one_node() {
            assertThat(BeanClass.toChainNodes("a"))
                    .containsOnly("a");
        }

        @Test
        void two_node() {
            assertThat(BeanClass.toChainNodes("a.b"))
                    .containsOnly("a", "b");
        }

        @Test
        void with_array_index() {
            assertThat(BeanClass.toChainNodes("a[0].b"))
                    .containsOnly("a", 0, "b");
        }
    }

    @Nested
    class GetValue {

        @Test
        void get_value_from_property() {
            assertThat(BeanClass.create(Bean.class).getPropertyChainValue(new Bean().setIntValue(10), "intValue"))
                    .isEqualTo(10);

            assertThat(BeanClass.create(Bean.class).getPropertyChainValue(new Bean().setBean(new Bean().setIntValue(10)), "bean.intValue"))
                    .isEqualTo(10);
        }

        @Test
        void get_value_from_collection() {
            assertThat(BeanClass.create(Bean.class).getPropertyChainValue(new Bean().setIntValues(new int[]{10}), "intValues[0]"))
                    .isEqualTo(10);
        }

        @Test
        void get_value_from_collection_element_property() {
            assertThat(BeanClass.create(Bean.class).getPropertyChainValue(new Bean().setBeans(new Bean[]{null}), "beans[0]")).isNull();

            assertThat(BeanClass.create(Bean.class).getPropertyChainValue(new Bean().setBeans(new Bean[]{new Bean().setIntValue(10)}), "beans[0].intValue"))
                    .isEqualTo(10);
        }

        @Nested
        class ChainBreak {

            @Test
            void raise_error_when_object_is_null() {
                NullPointerInChainException exception = assertThrows(NullPointerInChainException.class,
                        () -> BeanClass.create(Bean.class).getPropertyChainValue(null, "intValue"));

                assertThat(exception).hasMessageContaining("Failed to read value at property chain: <.intValue>");

                exception = assertThrows(NullPointerInChainException.class,
                        () -> BeanClass.create(Bean.class).getPropertyChainValue(null, "bean.intValue"));

                assertThat(exception).hasMessageContaining("Failed to read value at property chain: <.bean>.intValue");
            }

            @Test
            void raise_error_when_array_index_out_of_range() {
                NullPointerInChainException exception = assertThrows(NullPointerInChainException.class,
                        () -> BeanClass.create(Bean.class).getPropertyChainValue(new Bean().setIntValues(new int[0]), "intValues[1]"));

                assertThat(exception).hasMessageContaining("Failed to read value at property chain: .intValues<[1]>");
            }

            @Test
            void raise_error_when_array_element_is_null() {
                NullPointerInChainException exception = assertThrows(NullPointerInChainException.class,
                        () -> BeanClass.create(Bean.class).getPropertyChainValue(new Bean().setBeans(new Bean[]{null}), "beans[0].intValue"));

                assertThat(exception).hasMessageContaining("Failed to read value at property chain: .beans[0]<.intValue>");
            }
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    class Bean {
        private int intValue;
        private Bean bean;
        private int[] intValues;
        private Bean[] beans;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    class Beans {
        private Bean[] bean;
        private Bean[] beans;
    }
}
