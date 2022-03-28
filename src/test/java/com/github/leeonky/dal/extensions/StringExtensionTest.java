package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.createTempFile;
import static org.assertj.core.api.Assertions.assertThat;

class StringExtensionTest {

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