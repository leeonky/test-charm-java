package com.github.leeonky.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeanClassTest {

    @Test
    void get_type() {
        assertThat(new BeanClass<>(String.class).getType()).isEqualTo(String.class);
    }

    @Test
    void get_name() {
        assertThat(new BeanClass<>(String.class).getName()).isEqualTo(String.class.getName());
    }

    @Test
    void get_simple_name() {
        assertThat(new BeanClass<>(String.class).getSimpleName()).isEqualTo(String.class.getSimpleName());
    }

    @Test
    void new_instance() {
        assertThat(new BeanClass<>(String.class).newInstance()).isEqualTo("");
    }

    @Test
    void new_instance_with_arg() {
        assertThat(new BeanClass<>(String.class).newInstance("hello")).isEqualTo("hello");
    }

    @Test
    void new_instance_failed_when_no_candidate_constructor() {
        assertThrows(IllegalArgumentException.class, () -> new BeanClass<>(BeanClassTest.class).newInstance("hello"));
    }

    @Test
    void support_get_class_name() {
        assertThat(BeanClass.getClassName("")).isEqualTo(String.class.getName());

        assertThat(BeanClass.getClassName(null)).isEqualTo(null);
    }
}