package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.leeonky.dal.extensions.StringExtension.StaticMethods.lines;
import static java.nio.file.Files.createTempFile;
import static org.assertj.core.api.Assertions.assertThat;

class StringExtensionTest {

    @Nested
    class ToString {

        @SneakyThrows
        @Test
        void byte_array_to_string() {
            DAL dal = DAL.getInstance();
            assertThat((Object) dal.evaluate("hello".getBytes(), "string")).isEqualTo("hello");
        }

        @SneakyThrows
        @Test
        void input_stream_to_string() {
            DAL dal = DAL.getInstance();
            assertThat((Object) dal.evaluate(new ByteArrayInputStream("hello".getBytes()), "string")).isEqualTo("hello");
        }

        @SneakyThrows
        @Test
        void file_to_string() {
            DAL dal = DAL.getInstance();
            Path tempFile = createTempFile("", "");
            File file = tempFile.toFile();
            Files.write(tempFile, "hello".getBytes());
            assertThat((Object) dal.evaluate(file, "string")).isEqualTo("hello");
            file.delete();
        }

        @SneakyThrows
        @Test
        void path_to_string() {
            DAL dal = DAL.getInstance();
            Path tempFile = createTempFile("", "");
            File file = tempFile.toFile();
            Files.write(tempFile, "hello".getBytes());
            assertThat((Object) dal.evaluate(tempFile, "string")).isEqualTo("hello");
            file.delete();
        }
    }

    @Nested
    class Lines {

        @Test
        void string_to_lines() {
            assertThat(lines("a")).containsExactly("a");
        }

        @Test
        void _r_n_to_lines() {
            assertThat(lines("a\r\nb\r\nc")).containsExactly("a", "b", "c");
        }

        @Test
        void _n_r_to_lines() {
            assertThat(lines("a\n\rb\n\rc")).containsExactly("a", "b", "c");
        }

        @Test
        void _n_to_lines() {
            assertThat(lines("a\nb\nc")).containsExactly("a", "b", "c");
        }

        @Test
        void _r_to_lines() {
            assertThat(lines("a\rb\rc")).containsExactly("a", "b", "c");
        }

        @Test
        void string_lines() {
            assertThat(lines("a\rb\r\nc\n\rd\ne")).containsExactly("a", "b", "c", "d", "e");
        }
    }
}
