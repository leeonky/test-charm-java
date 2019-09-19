package com.github.leeonky.map.spec.map;

import com.github.leeonky.map.Mapper;
import com.github.leeonky.map.Mapping;
import com.github.leeonky.map.MappingFrom;
import com.github.leeonky.map.MappingView;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MappingAnnotation {
    private final Mapper mapper = new Mapper(getClass().getPackage().getName());

    public static class Entity {
        public int i = 1;
        public int j = 2;
    }

    public static class AnotherEntity {
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

    @MappingFrom(AnotherEntity.class)
    public static class DtoSuperView extends Dto {

    }

    @Nested
    class GuessMappingFrom {

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
    }

    @Nested
    class GuessMappingView {

        @Test
        void should_get_mapping_view_class_from_current_class_mapping_annotation() {
            Object dto = mapper.map(new Entity(), Dto.class);

            assertThat(dto).hasFieldOrPropertyWithValue("j", 2);
        }

        @Test
        void should_get_mapping_from_class_from_current_class_mapping_from_annotation() {
            Object dto = mapper.map(new Entity(), DtoDirectlyFrom.class);

            assertThat(dto).hasFieldOrPropertyWithValue("i", 1);
        }

        @Test
        void should_not_guess_view_from_supper_class() {
            Object dto = mapper.map(new AnotherEntity(), Dto.class);

            assertThat(dto).isNull();
        }
    }
}
