package org.testcharm.util.function;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TriConsumerTest {

    @Test
    void and_then() {
        StringBuilder sb = new StringBuilder();
        TriConsumer<String, String, String> consumer1 = (a, b, c) -> sb.append(a);
        TriConsumer<String, String, String> consumer2 = (a, b, c) -> sb.append(b);
        TriConsumer<String, String, String> consumer3 = (a, b, c) -> sb.append(c);

        TriConsumer<String, String, String> combined = consumer1.andThen(consumer2).andThen(consumer3);

        combined.accept("A", "B", "C");

        assertEquals("ABC", sb.toString());
    }
}