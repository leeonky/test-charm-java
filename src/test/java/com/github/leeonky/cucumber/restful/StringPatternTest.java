package com.github.leeonky.cucumber.restful;

import org.junit.jupiter.api.Test;

import static com.github.leeonky.cucumber.restful.StringPattern.replaceAll;
import static org.assertj.core.api.Assertions.assertThat;

public class StringPatternTest {
    @Test
    void should_do_nothing_for_empty_string() {
        assertThat(replaceAll("", "", str -> "")).isEmpty();
    }

    @Test
    void should_support_no_matching_case() {
        assertThat(replaceAll("x", "a", str -> "")).isEqualTo("x");
    }

    @Test
    void should_match_one_pattern_without_capture() {
        assertThat(replaceAll("a", "a", str -> {
            assertThat(str).isEqualTo(null);
            return "x";
        })).isEqualTo("x");
    }

    @Test
    void should_keep_non_matched_before_matched() {
        assertThat(replaceAll("0a", "a", str -> {
            assertThat(str).isEqualTo(null);
            return "x";
        })).isEqualTo("0x");
    }

    @Test
    void should_support_capture() {
        assertThat(replaceAll("a", "(a)", str -> {
            assertThat(str).isEqualTo("a");
            return str.toUpperCase();
        })).isEqualTo("A");
    }

    @Test
    void should_support_capture_and_replace_all_content() {
        assertThat(replaceAll("0a1b2c3", "([abc])", String::toUpperCase))
                .isEqualTo("0A1B2C3");
    }
}