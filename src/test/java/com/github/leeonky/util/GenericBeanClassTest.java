package com.github.leeonky.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class GenericBeanClassTest {

    @Test
    void should_return_generic_type_info() {
        BeanClass<?> beanClass = BeanClass.create(GenericType.createGenericType(StringList.class.getGenericSuperclass()));

        assertThat(beanClass.hasTypeArguments()).isTrue();
        assertThat(beanClass.getTypeArguments(0).get()).isEqualTo(BeanClass.create(String.class));
    }

    @Test
    void hash_code() {
        GenericType genericType = GenericType.createGenericType(StringList.class.getGenericSuperclass());
        assertThat(BeanClass.create(genericType).hashCode())
                .isEqualTo(Objects.hash(GenericBeanClass.class, genericType));
    }

    @Test
    void bean_class_equal() {
        assertThat(new GenericBeanClass<>(GenericType.createGenericType(StringList.class.getGenericSuperclass())))
                .isEqualTo(new GenericBeanClass<>(GenericType.createGenericType(StringList.class.getGenericSuperclass())));
    }

    public static class StringList extends ArrayList<String> {
    }
}
