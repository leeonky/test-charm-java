package com.github.leeonky.dal.extensions;

import org.junit.jupiter.api.Test;

import static com.github.leeonky.dal.extensions.TextExtension.StaticMethods.lines;
import static org.assertj.core.api.Assertions.assertThat;

public class TextExtensionTest {

    @Test
    void string_to_lines() {
        assertThat(lines("a")).containsExactly("a");
    }

    @Test
    void _r_n_to_lines() {
        assertThat(lines("a\r\nb\r\nc")).containsExactly("a", "b", "c");
    }

    @Test
    void _n_r_to_lines() {
        assertThat(lines("a\n\rb\n\rc")).containsExactly("a", "b", "c");
    }

    @Test
    void _n_to_lines() {
        assertThat(lines("a\nb\nc")).containsExactly("a", "b", "c");
    }

    @Test
    void _r_to_lines() {
        assertThat(lines("a\rb\rc")).containsExactly("a", "b", "c");
    }
}
