package com.github.leeonky.map.bug;

import com.github.leeonky.map.FromProperty;
import com.github.leeonky.map.Mapper;
import com.github.leeonky.map.MappingFrom;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlatMapFromEmptyList {
    private Mapper mapper = new Mapper("com.github.leeonky.map.bug");

    @Test
    void should_return_empty_list_when_source_list_is_empty() {
        SourceDTO data = mapper.map(new Source(), SourceDTO.class);

        assertThat(data.names).isEqualTo(emptyList());
    }

    @Test
    void should_raise_error_when_des_type_is_invalid() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> mapper.map(new Source(), InvalidSourceDTO.class));

        assertThat(exception).hasMessage("Type of 'com.github.leeonky.map.bug.FlatMapFromEmptyList$InvalidSourceDTO.names' is invalid, expect Iterable or Array");
    }

    public static class Source {
        public List<Element> elements = emptyList();
    }

    public static class Element {
        public String name;
    }

    @MappingFrom(Source.class)
    public static class SourceDTO {

        @FromProperty("elements{name}")
        public List<String> names;
    }

    @MappingFrom(Source.class)
    public static class InvalidSourceDTO {

        @FromProperty("elements{name}")
        public String names;
    }
}
