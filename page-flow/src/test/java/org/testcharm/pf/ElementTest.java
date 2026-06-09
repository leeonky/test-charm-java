package org.testcharm.pf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.dal.DAL;
import org.testcharm.util.Sneaky;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ElementTest {
    private final DAL dal = mock(DAL.class);
    private final Element<?, ?, ?> element = spy(Element.class);
    private final PageFlow pageFlow = mock(PageFlow.class);

    @BeforeEach
    void setUp() {
        when(element.pageFlow()).thenReturn(Sneaky.cast(pageFlow));
        when(pageFlow.dal()).thenReturn(dal);
    }

    @Nested
    class DefaultMethod {

        @Test
        void default_is_input_is_false() {
            assertFalse(element.isInput());
        }

        @Test
        void default_value_not_support() {
            assertThrows(UnsupportedOperationException.class, element::value);
        }
    }

    @Nested
    class Perform {

        @Test
        void perform_with_vars() {
            Object vars = new Object();
            element.perform("action", vars);

            verify(dal).evaluate(argThat(argument -> {
                assertEquals(element, Sneaky.get(argument));
                return true;
            }), eq("action"), isNull(), eq(vars));
        }

        @Test
        void perform_without_vars() {
            element.perform("action");

            verify(dal).evaluate(argThat(argument -> {
                assertEquals(element, Sneaky.get(argument));
                return true;
            }), eq("action"), isNull(), isNull());
        }

        @Test
        void perform_all_with_vars() {
            Object vars = new Object();
            element.performAll("actions", vars);

            verify(dal).evaluateAll(argThat(argument -> {
                assertEquals(element, Sneaky.get(argument));
                return true;
            }), eq("actions"), eq(vars));
        }

        @Test
        void perform_all_without_vars() {
            element.performAll("actions");

            verify(dal).evaluateAll(argThat(argument -> {
                assertEquals(element, Sneaky.get(argument));
                return true;
            }), eq("actions"), isNull());
        }
    }

    @Nested
    class Should {

        @Test
        void should_with_vars() {
            Object vars = new Object();
            element.should("expression", vars);

            verify(dal).evaluate(argThat(argument -> {
                assertEquals(element, Sneaky.get(argument));
                return true;
            }), eq("expression"), isNull(), eq(vars));
        }

        @Test
        void should_without_vars() {
            element.should("expression");

            verify(dal).evaluate(argThat(argument -> {
                assertEquals(element, Sneaky.get(argument));
                return true;
            }), eq("expression"), isNull(), isNull());
        }
    }

    @Nested
    class Locate {

        @Test
        void locate_with_vars() {
            Object vars = new Object();
            when(dal.evaluate(any(), any(), any(), any())).thenReturn(mock(Elements.class));

            element.locate("expression", vars);

            verify(dal).evaluate(argThat(argument -> {
                assertEquals(element, Sneaky.get(argument));
                return true;
            }), eq("expression"), isNull(), eq(vars));
        }

        @Test
        void locate_without_vars() {
            when(dal.evaluate(any(), any(), any(), any())).thenReturn(mock(Elements.class));

            element.locate("expression");

            verify(dal).evaluate(argThat(argument -> {
                assertEquals(element, Sneaky.get(argument));
                return true;
            }), eq("expression"), isNull(), isNull());
        }

        @Test
        void raise_exception_when_locate_return_wrong_type() {
            when(dal.evaluate(any(), any(), any(), any())).thenReturn(new Object());

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> element.locate("expression"));
            assertTrue(exception.getMessage().contains("Locate should return type Elements, but got:"));
        }
    }
}
