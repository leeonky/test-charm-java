package com.github.leeonky.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static com.github.leeonky.util.CollectionHelper.toStream;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CollectionHelperTest {

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
    }

//    TODO get access size collection
//    TODO convert collection
//    TODO equal collection
}
