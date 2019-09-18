package com.github.leeonky.map.spec.map;

import com.github.leeonky.map.Mapper;
import com.github.leeonky.map.Mapping;
import com.github.leeonky.map.MappingFrom;
import com.github.leeonky.map.MappingView;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MappingAnnotation {
    private final Mapper mapper = new Mapper(getClass().getPackage().getName());

    @Test
    void should_get_mapping_from_class_from_current_class_mapping_annotation() {
        Object dto = mapper.map(new Entity(), Dto.class);

        assertThat(dto).hasFieldOrPropertyWithValue("j", 2);
    }

    @Test
    void should_get_mapping_from_class_from_current_class_mapping_from_annotation() {
        Object dto = mapper.map(new Entity(), DtoDirectlyFrom.class);

        assertThat(dto).hasFieldOrPropertyWithValue("i", 1);
    }

    @Test
    void should_get_mapping_from_class_from_declaring_class_mapping_from() {
        Object dto = mapper.map(new Entity(), Dto.DtoFromDeclaring.class);

        assertThat(dto).hasFieldOrPropertyWithValue("j", 2);
    }

    @Test
    void should_get_mapping_from_class_from_super_class_mapping_from() {
        Object dto = mapper.map(new Entity(), DtoFromSuper.class);

        assertThat(dto).hasFieldOrPropertyWithValue("j", 2);
    }

    public static class Entity {
        public int i = 1;
        public int j = 2;
    }

    @Mapping(from = Entity.class, view = Dto.class)
    public static class Dto {
        public int j;

        public static class DtoFromDeclaring {
            public int j;
        }
    }

    @MappingView(DtoFromSuper.class)
    public static class DtoFromSuper extends Dto {
    }

    @Mapping(from = String.class, view = DtoDirectlyFrom.class)
    @MappingFrom(Entity.class)
    public static class DtoDirectlyFrom {
        public int i;
    }
}
