package com.github.leeonky.map.spec.map;

import com.github.leeonky.map.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MappingAnnotation {
    private final Mapper mapper = new Mapper(getClass().getPackage().getName());

    interface Scope1 {
    }

    interface Scope2 {
    }

    interface Scope3 {
    }

    interface Scope4 {
    }

    public static class Entity {
        public int f1 = 1, f2 = 2, f3 = 3, f4 = 4, f5 = 5;
    }

    @Mapping(from = Entity.class, view = EntityFromDto1.class)
    public static class EntityFromDto1 {
        public int f1;
    }

    @Mapping(from = String.class, view = EntityFromDto2.class)
    @MappingFrom(Entity.class)
    public static class EntityFromDto2 {
        public int f2;
    }

    @MappingFrom(Entity.class)
    public static class EntityFromDto3 {
        public static class Dto {
            public int f3;
        }
    }

    public static class EntityFromDto4 extends EntityFromDto3 {
        public int f4;
    }

    @Mapping(from = Entity.class, view = String.class)
    @MappingView(EntityViewDto1.class)
    public static class EntityViewDto1 {
        public int f1;
    }

    @MappingView(EntityViewDto2.class)
    public static class EntityViewDto2 {
    }

    @MappingFrom(Entity.class)
    public static class EntityViewDto3 extends EntityViewDto2 {
        public int f2;
    }

    @Mapping(from = Entity.class, view = View.Simple.class, scope = Scope1.class)
    public static class EntityScopeDto1 {
        public int f1;
    }

    @Mapping(from = Entity.class, view = View.Simple.class)
    public static class EntityScopeDto2 {
        public int f2;
    }

    @Mapping(from = Entity.class, view = View.Simple.class, scope = Scope1.class)
    @MappingScope(Scope2.class)
    public static class EntityScopeDto3 {
        public int f3;
    }

    @MappingScope(Scope3.class)
    public static class EntityScopeDto4 {
        @Mapping(from = Entity.class, view = View.Simple.class)
        public static class EntityScopeDto5 {
            public int f4;
        }
    }

    @Mapping(from = Entity.class, view = View.Detail.class)
    public static class EntityScopeDto6 extends EntityScopeDto4 {
        public int f4;
    }

    @Mapping(from = Entity.class, view = View.Detail.class)
    public static class EntityScopeDto7 {
        public int f3;
    }


    @Nested
    class GuessMappingFrom {

        @Test
        void should_get_mapping_from_class_from_current_class_mapping_annotation() {
            Object o = mapper.map(new Entity(), EntityFromDto1.class);

            assertThat(o)
                    .isInstanceOf(EntityFromDto1.class)
                    .hasFieldOrPropertyWithValue("f1", 1);
        }

        @Test
        void should_get_mapping_from_class_from_current_class_mapping_from_annotation() {
            Object o = mapper.map(new Entity(), EntityFromDto2.class);

            assertThat(o)
                    .isInstanceOf(EntityFromDto2.class)
                    .hasFieldOrPropertyWithValue("f2", 2);
        }

        @Test
        void should_get_mapping_from_class_from_declaring_class() {
            Object o = mapper.map(new Entity(), EntityFromDto3.Dto.class);

            assertThat(o).hasFieldOrPropertyWithValue("f3", 3);
        }

        @Test
        void should_get_mapping_from_class_from_super_class() {
            Object o = mapper.map(new Entity(), EntityFromDto4.class);

            assertThat(o).hasFieldOrPropertyWithValue("f4", 4);
        }
    }

    @Nested
    class GuessMappingView {

        @Test
        void should_get_mapping_view_class_from_current_class_mapping_annotation() {
            Object o = mapper.map(new Entity(), EntityFromDto1.class);

            assertThat(o)
                    .isInstanceOf(EntityFromDto1.class)
                    .hasFieldOrPropertyWithValue("f1", 1);
        }

        @Test
        void should_get_mapping_from_class_from_current_class_mapping_view_annotation() {
            Object o = mapper.map(new Entity(), EntityViewDto1.class);

            assertThat(o).hasFieldOrPropertyWithValue("f1", 1);
        }

        @Test
        // TODO to be removed feature
        @Deprecated
        void should_not_guess_view_from_supper_class() {
            Object o = mapper.map(new Entity(), EntityViewDto3.class);

            assertThat(o).hasFieldOrPropertyWithValue("f2", 2);
        }
    }

    @Nested
    class GuessMappingScope {

        @Test
        void should_get_mapping_scope_class_from_current_class_mapping_annotation() {
            mapper.setScope(Scope1.class);

            Object o = mapper.map(new Entity(), View.Simple.class);

            assertThat(o)
                    .isInstanceOf(EntityScopeDto1.class)
                    .hasFieldOrPropertyWithValue("f1", 1);
        }

        @Test
        void should_get_mapping_scope_class_from_current_class_mapping_scope_annotation() {
            mapper.setScope(Scope2.class);

            Object dto = mapper.map(new Entity(), View.Simple.class);

            assertThat(dto)
                    .isInstanceOf(EntityScopeDto3.class)
                    .hasFieldOrPropertyWithValue("f3", 3);
        }

        @Test
        void should_get_mapping_scope_class_from_declaring_class() {
            mapper.setScope(Scope3.class);

            Object o = mapper.map(new Entity(), View.Simple.class);

            assertThat(o)
                    .isInstanceOf(EntityScopeDto4.EntityScopeDto5.class)
                    .hasFieldOrPropertyWithValue("f4", 4);
        }

        @Test
        void should_get_mapping_scope_class_from_super_class() {
            mapper.setScope(Scope3.class);

            Object dto = mapper.map(new Entity(), View.Detail.class);

            assertThat(dto)
                    .isInstanceOf(EntityScopeDto6.class)
                    .hasFieldOrPropertyWithValue("f4", 4);
        }
    }
}
