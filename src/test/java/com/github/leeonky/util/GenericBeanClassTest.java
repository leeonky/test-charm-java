package com.github.leeonky.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class GenericBeanClassTest {

    @Test
    void should_return_generic_type_info() {
        BeanClass<?> beanClass = BeanClass.create(GenericType.createGenericType(StringList.class.getGenericSuperclass()));

        assertThat(beanClass.hasTypeArguments()).isTrue();
        assertThat(beanClass.getTypeArguments(0).get()).isEqualTo(BeanClass.create(String.class));
    }

    public static class StringList extends ArrayList<String> {
    }
}
