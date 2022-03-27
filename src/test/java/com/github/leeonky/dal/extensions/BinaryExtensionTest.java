package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import io.cucumber.messages.internal.com.google.common.io.Files;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Path;

import static java.nio.file.Files.createTempFile;
import static org.assertj.core.api.Assertions.assertThat;

class BinaryExtensionTest {

    @SneakyThrows
    @Test
    void input_stream_to_binary() {
        DAL dal = DAL.getInstance();
        assertThat((Object) dal.evaluate(new ByteArrayInputStream("hello".getBytes()), "binary"))
                .isEqualTo("hello".getBytes());
    }

    @SneakyThrows
    @Test
    void file_to_binary() {
        DAL dal = DAL.getInstance();
        Path tempFile = createTempFile("", "");
        File file = tempFile.toFile();
        Files.write("hello".getBytes(), file);
        assertThat((Object) dal.evaluate(file, "binary")).isEqualTo("hello".getBytes());
        file.delete();
    }

    @SneakyThrows
    @Test
    void path_to_binary() {
        DAL dal = DAL.getInstance();
        Path tempFile = createTempFile("", "");
        File file = tempFile.toFile();
        Files.write("hello".getBytes(), file);
        assertThat((Object) dal.evaluate(tempFile, "binary")).isEqualTo("hello".getBytes());
        file.delete();
    }
}