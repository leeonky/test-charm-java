package org.testcharm.dal.extensions.basic;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.dal.DAL;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.createTempFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testcharm.dal.extensions.basic.text.Methods.json;

class TextExtensionTest {

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
        Files.write(tempFile, "{\"a\": 1}".getBytes());
        assertThat((Map) dal.evaluate(tempFile.toFile(), "json")).isEqualTo(new HashMap<String, Object>() {{
            put("a", 1);
        }});
        tempFile.toFile().delete();
    }

    @SneakyThrows
    @Test
    void path_to_json() {
        DAL dal = DAL.getInstance();
        Path tempFile = createTempFile("", "");
        Files.write(tempFile, "{\"a\": 1}".getBytes());
        assertThat((Map) dal.evaluate(tempFile, "json")).isEqualTo(new HashMap<String, Object>() {{
            put("a", 1);
        }});
        tempFile.toFile().delete();
    }

    @Nested
    class JsonToMap {

        @Test
        void null_to_null() {
            assertThat(json("null")).isEqualTo(null);
        }

        @Test
        void number_to_number() {
            assertThat(json("1")).isEqualTo(1);
        }

        @Test
        void boolean_to_boolean() {
            assertThat(json("true")).isEqualTo(true);
        }

        @Test
        void list_to_list() {
            assertThat(json("[]")).isEqualTo(Collections.emptyList());
        }
    }
}