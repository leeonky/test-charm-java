package com.github.leeonky.dal.extensions;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.leeonky.dal.extensions.FileExtension.formatFileSize;
import static org.assertj.core.api.Assertions.assertThat;

class FileExtensionTest {

    @Nested
    class FormatFileSize {

        @Test
        void less_than_9999_byte() {
            assertThat(formatFileSize(1)).isEqualTo("1");
            assertThat(formatFileSize(9999)).isEqualTo("9999");
        }

        @Test
        void less_than_10000_byte() {
            assertThat(formatFileSize(10000)).isEqualTo("9.8K");
        }

        @Test
        void less_than_1000K() {
            assertThat(formatFileSize(100000)).isEqualTo("97.7K");
            assertThat(formatFileSize(1024000 - 100)).isEqualTo("999.9K");
        }

        @Test
        void less_than_1000M() {
            assertThat(formatFileSize(10240000)).isEqualTo("9.8M");
            assertThat(formatFileSize(1024000000)).isEqualTo("976.6M");
        }

        @Test
        void less_than_1000G() {
            assertThat(formatFileSize(10240000000L)).isEqualTo("9.5G");
        }

        @Test
        void less_than_1000T() {
            assertThat(formatFileSize(10240000000000L)).isEqualTo("9.3T");
        }
    }
}