package org.testcharm.pf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.dal.DAL;
import org.testcharm.util.Sneaky;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PanelTest {
    private final DAL dal = mock(DAL.class);
    private final Panel<?> panel = spy(Panel.class);
    private final Element<?, ?, ?> element = mock(Element.class);
    private final PageFlow pageFlow = mock(PageFlow.class);

    @BeforeEach
    void setUp() {
        when(element.pageFlow()).thenReturn(Sneaky.cast(pageFlow));
        when(pageFlow.dal()).thenReturn(dal);
        when(panel.element()).thenReturn(Sneaky.cast(element));
    }

    @Nested
    class Methods {
        @Test
        void text_should_return_element_text() {
            when(element.text()).thenReturn("element text");

            assertEquals("element text", panel.text());
        }
    }

    @Nested
    class Perform {

        @Test
        void perform_with_vars() {
            Object vars = new Object();
            panel.perform("action", vars);

            verify(dal).evaluate(argThat(argument -> {
                assertEquals(element, Sneaky.get(argument));
                return true;
            }), eq("action"), isNull(), eq(vars));
        }

        @Test
        void perform_without_vars() {
            panel.perform("action");

            verify(dal).evaluate(argThat(argument -> {
                assertEquals(element, Sneaky.get(argument));
                return true;
            }), eq("action"), isNull(), isNull());
        }

        @Test
        void perform_all_with_vars() {
            Object vars = new Object();
            panel.performAll("actions", vars);

            verify(dal).evaluateAll(argThat(argument -> {
                assertEquals(element, Sneaky.get(argument));
                return true;
            }), eq("actions"), eq(vars));
        }

        @Test
        void perform_all_without_vars() {
            panel.performAll("actions");

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
            panel.should("expression", vars);

            verify(dal).evaluate(argThat(argument -> {
                assertEquals(panel, Sneaky.get(argument));
                return true;
            }), eq("expression"), isNull(), eq(vars));
        }

        @Test
        void should_without_vars() {
            panel.should("expression");

            verify(dal).evaluate(argThat(argument -> {
                assertEquals(panel, Sneaky.get(argument));
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

            panel.locate("expression", vars);

            verify(dal).evaluate(argThat(argument -> {
                assertEquals(element, Sneaky.get(argument));
                return true;
            }), eq("expression"), isNull(), eq(vars));
        }

        @Test
        void locate_without_vars() {
            when(dal.evaluate(any(), any(), any(), any())).thenReturn(mock(Elements.class));

            panel.locate("expression");

            verify(dal).evaluate(argThat(argument -> {
                assertEquals(element, Sneaky.get(argument));
                return true;
            }), eq("expression"), isNull(), isNull());
        }

        @Test
        void raise_exception_when_locate_return_wrong_type() {
            when(dal.evaluate(any(), any(), any(), any())).thenReturn(new Object());

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> panel.locate("expression"));
            assertTrue(exception.getMessage().contains("Locate should return type Elements, but got:"));
        }
    }

    @Nested
    class Operate {
        @Test
        void operate_with_vars() {
            Object vars = new Object();
            panel.operate("operation", vars);

            verify(dal).evaluate(argThat(argument -> {
                assertEquals(panel, Sneaky.get(argument));
                return true;
            }), eq("operation"), isNull(), eq(vars));
        }

        @Test
        void operate_without_vars() {
            panel.operate("operation");

            verify(dal).evaluate(argThat(argument -> {
                assertEquals(panel, Sneaky.get(argument));
                return true;
            }), eq("operation"), isNull(), isNull());
        }

        @Test
        void operate_all_with_vars() {
            Object vars = new Object();
            panel.operateAll("operations", vars);

            verify(dal).evaluateAll(argThat(argument -> {
                assertEquals(panel, Sneaky.get(argument));
                return true;
            }), eq("operations"), eq(vars));
        }

        @Test
        void operate_all_without_vars() {
            panel.operateAll("operations");

            verify(dal).evaluateAll(argThat(argument -> {
                assertEquals(panel, Sneaky.get(argument));
                return true;
            }), eq("operations"), isNull());
        }
    }
}