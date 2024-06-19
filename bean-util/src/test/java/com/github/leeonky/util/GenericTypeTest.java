package com.github.leeonky.util;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class GenericTypeTest {

    @Test
    void hash_code() {
        GenericType genericType = GenericType.createGenericType(Integer.class);
        assertThat(genericType.hashCode())
                .isEqualTo(Objects.hash(GenericType.class, Integer.class));
    }

    @Test
    void bean_class_equal() {
        assertThat(GenericType.createGenericType(Integer.class))
                .isEqualTo(GenericType.createGenericType(Integer.class));
    }
}