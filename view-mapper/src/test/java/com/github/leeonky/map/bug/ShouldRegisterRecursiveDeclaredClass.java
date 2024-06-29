package com.github.leeonky.map.bug;

import com.github.leeonky.map.Mapper;
import com.github.leeonky.map.MappingFrom;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShouldRegisterRecursiveDeclaredClass {

    @Test
    void should_register_recursive_declared_classes() {
        Mapper mapper = new Mapper("com.github.leeonky.map.bug");

        Object o = mapper.map(new Entity(), DTO.Simple.Summary.class);

        assertThat(o).isInstanceOf(DTO.Simple.Summary.class);
    }

    public static class Entity {
    }

    @MappingFrom(Entity.class)
    public static class DTO {
        public static class Simple {
            public static class Summary {
            }
        }
    }
}
