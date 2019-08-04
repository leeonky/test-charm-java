package com.github.leeonky.map.spec;

import com.github.leeonky.map.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BasicMapping {
    private final Mapper mapper = new Mapper(getClass().getPackage().getName());
    private final Book javaProgrammingBook = new Book().setName("Java Programming").setPrice(new BigDecimal(100));

    @Test
    void map_object_via_view() {
        assertThat(mapper.<Object>map(javaProgrammingBook, Simple.class))
                .isInstanceOf(SimpleBookDTO.class)
                .hasFieldOrPropertyWithValue("name", "Java Programming");
    }

    @Test
    void support_specify_mapping_view_in_sub_mapping_class() {
        assertThat(mapper.<Object>map(javaProgrammingBook, Detail.class))
                .isInstanceOf(DetailBookDTO.class)
                .hasFieldOrPropertyWithValue("name", "Java Programming")
                .hasFieldOrPropertyWithValue("price", new BigDecimal(100));
    }

    @Test
    void map_object_via_view_and_scope() {
        mapper.setScope(FrontEnd.class);

        assertThat(mapper.<Object>map(javaProgrammingBook, Simple.class))
                .isInstanceOf(FrontEndSimpleBookDTO.class)
                .hasFieldOrPropertyWithValue("name", "Java Programming")
                .hasFieldOrPropertyWithValue("price", "100");
    }

    @Test
    void should_return_null_when_source_class_not_register() {
        String notRegisterClassInstance = "";
        assertThat((Object) mapper.map(notRegisterClassInstance, Simple.class)).isNull();
    }

    @Test
    void should_return_null_when_view_not_register() {
        assertThat((Object) mapper.map(javaProgrammingBook, String.class)).isNull();
    }

    @Test
    void null_object_mapping_result_should_be_null() {
        assertThat((Object) mapper.map(null, Object.class)).isNull();
        assertThat(mapper.mapTo(null, Object.class)).isNull();
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    static class Book {
        private String name;
        private BigDecimal price;
    }

    @Getter
    @Setter
    @Mapping(from = Book.class, view = Simple.class)
    static class SimpleBookDTO {
        private String name;
    }

    @Getter
    @Setter
    @Mapping(from = Book.class, view = Simple.class, scope = FrontEnd.class)
    static class FrontEndSimpleBookDTO {
        private String name;
        private String price;
    }

    @Getter
    @Setter
    @MappingView(Detail.class)
    static class DetailBookDTO extends SimpleBookDTO {
        private BigDecimal price;
    }

    private static class FrontEnd {
    }
}
