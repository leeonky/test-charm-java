package com.github.leeonky.map.bug;

import com.github.leeonky.map.Mapper;
import com.github.leeonky.map.Mapping;
import com.github.leeonky.map.MappingView;
import com.github.leeonky.map.View;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AvoidUseDeclaringClassSuperClassDefaultVoidScope {
    private Mapper mapper = new Mapper("com.github.leeonky.map.bug");

    @Test
    void avoid_use_declaring_default_void_scope() {
        Entity entity = new Entity();
        Optional<Class<?>> mapping1 = mapper.findMapping(entity.getClass(), View.Detail.class);
        mapper.setScope(Frontend.class);
        Optional<Class<?>> mapping2 = mapper.findMapping(entity.getClass(), View.Detail.class);

        assertThat(mapping1).isNotEqualTo(mapping2);
    }

    interface Frontend {
    }

    public static class Entity {
    }

    @Mapping(from = Entity.class, view = View.Summary.class, scope = Frontend.class)
    public static class FrontendSimpleEntity {
    }

    @MappingView(View.Detail.class)
    public static class FrontendDetailEntity extends FrontendSimpleEntity {
    }
}
