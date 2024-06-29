package com.github.leeonky.map.spec.map;

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
        assertThat(mapper.<Object>map(javaProgrammingBook, View.Summary.class))
                .isInstanceOf(SimpleBookDTO.class)
                .hasFieldOrPropertyWithValue("name", "Java Programming");

        assertThat(mapper.<Object>map(javaProgrammingBook, View.Detail.class))
                .isInstanceOf(DetailBookDTO.class)
                .hasFieldOrPropertyWithValue("name", "Java Programming")
                .hasFieldOrPropertyWithValue("price", new BigDecimal(100));
    }

    @Test
    void map_object_via_view_and_scope() {
        mapper.setScope(FrontEnd.class);

        assertThat(mapper.<Object>map(javaProgrammingBook, View.Summary.class))
                .isInstanceOf(FrontEndSimpleBookDTO.class)
                .hasFieldOrPropertyWithValue("name", "Java Programming")
                .hasFieldOrPropertyWithValue("price", new BigDecimal(100));
    }

    @Test
    void support_map_property_from_different_name() {
        assertThat(mapper.<Object>map(javaProgrammingBook, BookNameDTO.class))
                .isInstanceOf(BookNameDTO.class)
                .hasFieldOrPropertyWithValue("bookName", "Java Programming");
    }

    @Test
    void support_type_convert_in_mapping() {
        assertThat(mapper.<Object>map(javaProgrammingBook, BookPriceDTO.class))
                .hasFieldOrPropertyWithValue("price", "100");
    }

    @Test
    void support_map_property_with_both_public_field_and_getter_setter() {
        Bean bean = new Bean();
        bean.setPrivateField1(1);
        bean.setPrivateField2(2);
        bean.publicField1 = 3;
        bean.publicField2 = 4;

        BeanDTO beanDTO = mapper.map(bean, BeanDTO.class);
        assertThat(beanDTO.privateField1).isEqualTo(1);
        assertThat(beanDTO.privateField2).isEqualTo(2);
        assertThat(beanDTO.getPublicField1()).isEqualTo(3);
        assertThat(beanDTO.getPublicField2()).isEqualTo(4);
    }

    @Test
    void should_return_null_when_source_class_not_register() {
        String notRegisterClassInstance = "";
        assertThat((Object) mapper.map(notRegisterClassInstance, View.Summary.class)).isNull();
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
    public static class Book {
        private String name;
        private BigDecimal price;
    }

    @Getter
    @Setter
    @Mapping(from = Book.class, view = View.Summary.class)
    static class SimpleBookDTO {
        private String name;
    }

    @Getter
    @Setter
    @MappingFrom(Book.class)
    static class BookNameDTO {
        @FromProperty("name")
        private String bookName;
    }

    @MappingFrom(Book.class)
    static class BookPriceDTO {
        public String price;
    }

    @Getter
    @Setter
    @Mapping(from = Book.class, view = View.Summary.class, scope = FrontEnd.class)
    static class FrontEndSimpleBookDTO {
        private String name;
        private BigDecimal price;
    }

    @Getter
    @Setter
    @MappingView(View.Detail.class)
    static class DetailBookDTO extends SimpleBookDTO {
        private BigDecimal price;
    }

    private static class FrontEnd {
    }

    public static class Bean {
        public int publicField1;
        public int publicField2;

        @Getter
        @Setter
        private int privateField1;

        @Getter
        @Setter
        private int privateField2;
    }

    @MappingFrom(Bean.class)
    static class BeanDTO {
        public int privateField1;
        public int privateField2;
        @Getter
        @Setter
        private int publicField1;
        @Getter
        @Setter
        private int publicField2;
    }
}
