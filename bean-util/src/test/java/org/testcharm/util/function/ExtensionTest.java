package org.testcharm.util.function;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.testcharm.util.function.Extension.*;

class ExtensionTest {
    @Nested
    class Not {

        @Test
        void not_() {
            Predicate<String> predicate = str -> {
                assertThat(str).isEqualTo("given");
                return true;
            };

            assertThat(not(predicate).test("given")).isFalse();
        }
    }

    @Nested
    class NotAllowParallelReduce {

        @Test
        void should_raise_exception() {
            assertThrows(IllegalStateException.class, () -> notAllowParallelReduce().apply(null, null));
        }
    }

    @Nested
    class FirstPresent {

        @Nested
        public class UseSupplier {

            @Test
            void return_empty_when_all_supplier_empty() {
                assertThat(getFirstPresent(Optional::empty)).isEmpty();
                assertThat(getFirstPresent(Optional::empty, Optional::empty)).isEmpty();
            }

            @Test
            void return_option_value_when_present() {
                assertThat(getFirstPresent(Optional::empty, () -> Optional.of("hello"))).hasValue("hello");
            }

            @Test
            void return_first_option_value_and_ignore_others() {
                assertThat(getFirstPresent(Optional::empty, () -> Optional.of("hello"), () -> {
                    fail();
                    return Optional.of("any str");
                })).hasValue("hello");
            }
        }

        @Nested
        public class UseOptional {

            @Test
            void return_empty_when_all_supplier_empty() {
                assertThat(firstPresent(Optional.empty())).isEmpty();
                assertThat(firstPresent(Optional.empty(), Optional.empty())).isEmpty();
            }

            @Test
            void return_option_value_when_present() {
                assertThat(firstPresent(Optional.empty(), Optional.of("hello"))).hasValue("hello");
            }
        }
    }

    @Nested
    class FirstNonNull {

        @Test
        void return_null_when_all_input_null() {
            assertThat(If.firstNonNull(new Object[]{null})).isNull();
            assertThat((Object) If.firstNonNull(null, null)).isNull();
        }

        @Test
        void return_first_non_null() {
            assertThat(If.firstNonNull(1)).isEqualTo(1);
            assertThat(If.firstNonNull(null, 1)).isEqualTo(1);
            assertThat(If.firstNonNull(null, 1, 2)).isEqualTo(1);
        }
    }

    @Nested
    class Adapt {

        @Test
        void runnable_to_supplier() {
            StringBuilder sb = new StringBuilder();
            adapt(() -> sb.append("hello")).get();
            assertThat(sb.toString()).isEqualTo("hello");
        }

        @SneakyThrows
        @Test
        void runnable_to_throwing_supplier() {
            StringBuilder sb = new StringBuilder();
            adaptThrowing(() -> sb.append("hello")).get();
            assertThat(sb.toString()).isEqualTo("hello");
        }
    }
}
