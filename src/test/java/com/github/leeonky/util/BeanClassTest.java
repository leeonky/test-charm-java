package com.github.leeonky.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeanClassTest {

    @Test
    void get_type() {
        assertThat(BeanClass.create(String.class).getType()).isEqualTo(String.class);
    }

    @Test
    void get_name() {
        assertThat(BeanClass.create(String.class).getName()).isEqualTo(String.class.getName());
    }

    @Test
    void get_simple_name() {
        assertThat(BeanClass.create(String.class).getSimpleName()).isEqualTo(String.class.getSimpleName());
    }

    @Test
    void new_instance() {
        assertThat(BeanClass.create(String.class).newInstance()).isEqualTo("");
    }

    @Test
    void new_instance_with_arg() {
        assertThat(BeanClass.create(String.class).newInstance("hello")).isEqualTo("hello");
    }

    @Test
    void new_instance_failed_when_no_candidate_constructor() {
        assertThrows(IllegalArgumentException.class, () -> BeanClass.create(BeanClassTest.class).newInstance("hello"));
    }

    @Test
    void get_class_name() {
        assertThat(BeanClass.getClassName("")).isEqualTo(String.class.getName());

        assertThat(BeanClass.getClassName(null)).isEqualTo(null);
    }

    @Test
    void create_default_value() {
        assertThat(BeanClass.create(int.class).createDefault()).isEqualTo(0);
        assertThat(BeanClass.create(Integer.class).createDefault()).isNull();
    }

    @Test
    void get_generic_params() {
        assertThat(BeanClass.create(Integer.class).hasTypeArguments()).isFalse();
        assertThat(BeanClass.create(Integer.class).getTypeArguments(0)).isEmpty();
    }
}