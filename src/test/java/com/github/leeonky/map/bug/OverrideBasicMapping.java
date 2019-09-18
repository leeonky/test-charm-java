package com.github.leeonky.map.bug;

import com.github.leeonky.map.Mapper;
import com.github.leeonky.map.Mapping;
import com.github.leeonky.map.MappingFrom;
import com.github.leeonky.map.View;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OverrideBasicMapping {
    @Test
    void sub_class_could_use_mapping_from_override_super_class_mapping_annotation() {
        Mapper mapper = new Mapper("com.github.leeonky.map.bug");

        assertThat((Object) mapper.map(new Base(), View.Simple.class)).isInstanceOf(BaseVO.class);

        assertThat((Object) mapper.map(new Sub(), View.Simple.class)).isInstanceOf(SubVO.class);
    }

    public static class Base {

    }

    public static class Sub {

    }

    @Mapping(from = Base.class, view = View.Simple.class)
    public static class BaseVO {

    }

    @MappingFrom(Sub.class)
    public static class SubVO extends BaseVO {

    }
}
