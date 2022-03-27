package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import io.cucumber.messages.internal.com.google.common.io.Files;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.createTempFile;
import static org.assertj.core.api.Assertions.assertThat;

class JsonExtensionTest {

    @SneakyThrows
    @Test
    void byte_array_to_json() {
        DAL dal = DAL.getInstance();
        assertThat((Map) dal.evaluate("{\"a\": 1}".getBytes(), "json")).isEqualTo(new HashMap<String, Object>() {{
            put("a", 1);
        }});
    }

    @SneakyThrows
    @Test
    void string_to_json() {
        DAL dal = DAL.getInstance();
        assertThat((Map) dal.evaluate("{\"a\": 1}", "json")).isEqualTo(new HashMap<String, Object>() {{
            put("a", 1);
        }});
    }

    @SneakyThrows
    @Test
    void input_stream_array_to_json() {
        DAL dal = DAL.getInstance();
        assertThat((Map) dal.evaluate(new ByteArrayInputStream("{\"a\": 1}".getBytes()), "json")).isEqualTo(
                new HashMap<String, Object>() {{
                    put("a", 1);
                }});
    }

    @SneakyThrows
    @Test
    void file_to_json() {
        DAL dal = DAL.getInstance();
        Path tempFile = createTempFile("", "");
        File file = tempFile.toFile();
        Files.write("{\"a\": 1}".getBytes(), file);
        assertThat((Map) dal.evaluate(file, "json")).isEqualTo(new HashMap<String, Object>() {{
            put("a", 1);
        }});
    }

    @SneakyThrows
    @Test
    void path_to_json() {
        DAL dal = DAL.getInstance();
        Path tempFile = createTempFile("", "");
        File file = tempFile.toFile();
        Files.write("{\"a\": 1}".getBytes(), file);
        assertThat((Map) dal.evaluate(tempFile, "json")).isEqualTo(new HashMap<String, Object>() {{
            put("a", 1);
        }});
    }
}