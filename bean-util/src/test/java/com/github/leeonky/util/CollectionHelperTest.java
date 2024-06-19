package com.github.leeonky.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.leeonky.util.CollectionHelper.convert;
import static com.github.leeonky.util.CollectionHelper.toStream;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CollectionHelperTest {

    public ArrayList<String> stringList;

    @Nested
    class CollectionToStream {

        @Test
        void support_null_input() {
            assertThrows(CannotToStreamException.class, () -> toStream(null));
        }

        @Test
        void support_get_array_elements() {
            assertThat(toStream(new String[]{"hello", "world"}).collect(Collectors.toList()))
                    .isEqualTo(asList("hello", "world"));
        }

        @Test
        void support_get_collection_elements() {
            assertThat(toStream(asList("hello", "world")).collect(Collectors.toList()))
                    .isEqualTo(asList("hello", "world"));
        }

        @Test
        void support_get_stream_elements() {
            assertThat(toStream(asList("hello", "world").stream()).collect(Collectors.toList()))
                    .isEqualTo(asList("hello", "world"));
        }
    }

    @Nested
    class ConvertTo {

        @Test
        void null_convert_to_array_is_null() {
            assertThat(convert(null, BeanClass.create(Object[].class))).isNull();
        }

        @Test
        void null_convert_to_collection_type_is_null() {
            assertThat(convert(null, BeanClass.create(List.class))).isNull();
        }

        @Test
        void array_to_list_with_out_convert_element_type() {
            assertThat(convert(new int[]{1, 2, 3}, BeanClass.create(List.class))).isEqualTo(asList(1, 2, 3));
        }

        @Test
        void array_to_array_with_out_convert_element_type() {
            assertThat(convert(new String[]{"1", "2"}, BeanClass.create(String[].class))).containsExactly("1", "2");
        }

        @Test
        void list_to_list_with_out_convert_element_type() {
            assertThat(convert(asList("1", "2"), BeanClass.create(ArrayList.class))).containsExactly("1", "2");
        }

        @Test
        void list_to_array_with_out_convert_element_type() {
            assertThat(convert(asList("1", "2"), BeanClass.create(String[].class))).containsExactly("1", "2");
        }

        @Test
        void to_array_and_convert_element_type() {
            assertThat(convert(asList("1", "2"), BeanClass.create(Integer[].class))).containsExactly(1, 2);
        }

        @Test
        void to_list_and_convert_element_type() {
            assertThat((List) convert(asList(1, 2), BeanClass.create(CollectionHelperTest.class).getProperty("stringList").getWriterType())).containsExactly("1", "2");
        }

        @Test
        void to_primitive_element_array() {
            assertThat(convert(asList("1", "2"), BeanClass.create(int[].class)))
                    .isInstanceOf(int[].class)
                    .containsExactly(1, 2);
        }

        @Test
        void create_collection_from_interface() {
            assertThat(convert(asList("1", "2"), BeanClass.create(Set.class)))
                    .isInstanceOf(LinkedHashSet.class)
                    .containsExactly("1", "2");
        }
    }

    @Nested
    class CompareCollection {

        @Test
        void null_and_null_is_equal() {
            assertThat(CollectionHelper.equals(null, null)).isTrue();
            assertThat(CollectionHelper.equals(emptyList(), null)).isFalse();
        }

        @Test
        void array_and_array() {
            assertThat(CollectionHelper.equals(new int[]{1, 2}, new int[]{1, 2})).isTrue();
            assertThat(CollectionHelper.equals(new int[]{1, 2}, new int[]{1, 3})).isFalse();
            assertThat(CollectionHelper.equals(new int[]{1, 2}, new Integer[]{1, 2})).isTrue();
        }

        @Test
        void array_and_list() {
            assertThat(CollectionHelper.equals(new int[]{1, 2}, asList(1, 2))).isTrue();
            assertThat(CollectionHelper.equals(new int[]{1, 2}, asList(1, 3))).isFalse();
        }
    }
}
