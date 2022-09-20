package com.github.leeonky.util.function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class IfFactoryTest {

    @Nested
    class IfBlock {
        Predicate<String> predicate = mock(Predicate.class);

        @Nested
        class IfTrue {

            @BeforeEach
            void setTrue() {
                when(predicate.test(any())).thenReturn(true);
            }

            @Test
            void if_true() {
                assertThat(IfFactory.when(predicate).then("return").get("input")).hasValue("return");

                verify(predicate).test("input");
            }

            @Test
            void if_true_else() {
                assertThat(IfFactory.when(predicate).then("return").orElse("else").get("input")).isEqualTo("return");
                verify(predicate).test("input");
            }

            @Test
            void if_true_else_if() {
                Predicate<String> predicate2 = mock(Predicate.class);

                assertThat(IfFactory.when(predicate).then("return").when(predicate2).then("elseIf").get("input")).hasValue("return");
                verify(predicate).test("input");
                verify(predicate2, never()).test(any());
            }
        }

        @Nested
        class IfFalse {

            @BeforeEach
            void setFalse() {
                when(predicate.test(any())).thenReturn(false);
            }

            @Test
            void if_false() {
                assertThat(IfFactory.when(predicate).then("return").get("input")).isEmpty();
                verify(predicate).test("input");
            }

            @Test
            void if_false_else() {
                assertThat(IfFactory.when(predicate).then("return").orElse("else").get("input")).isEqualTo("else");
                verify(predicate).test("input");
            }

            @Nested
            class Else {
                Predicate<String> predicate2 = mock(Predicate.class);

                @Test
                void if_false_else_if() {
                    when(predicate2.test(any())).thenReturn(true);

                    assertThat(IfFactory.when(predicate).then("return").when(predicate2).then("elseIf").get("input")).hasValue("elseIf");
                    verify(predicate).test("input");
                    verify(predicate2).test("input");
                }

                @Test
                void if_false_else_if_true_else() {
                    when(predicate2.test(any())).thenReturn(true);

                    assertThat(IfFactory.when(predicate).then("return").when(predicate2).then("elseIf").orElse("else").get("input")).isEqualTo("elseIf");
                    verify(predicate).test("input");
                    verify(predicate2).test("input");
                }

                @Test
                void if_false_else_if_false_else() {
                    when(predicate2.test(any())).thenReturn(false);

                    assertThat(IfFactory.when(predicate).then("return").when(predicate2).then("elseIf").orElse("else").get("input")).isEqualTo("else");
                    verify(predicate).test("input");
                    verify(predicate2).test("input");
                }
            }
        }
    }
}
