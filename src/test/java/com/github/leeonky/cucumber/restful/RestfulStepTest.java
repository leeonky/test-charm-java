package com.github.leeonky.cucumber.restful;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static java.util.Arrays.asList;

public class RestfulStepTest {
    private RestfulStep restfulStep = new RestfulStep();
    private Steps steps = new Steps(restfulStep);

    @AfterEach
    void resetMockServer() {
        steps.stopMockServer();
    }

    @Nested
    class PostObject {

        @SneakyThrows
        @Test
        void post_single_value() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.post("/test", (Object) "hello");

            steps.verifyRequest(": [{path= '/test' body.rawBytes.base64.string= '\"hello\"'}]");
        }

        @SneakyThrows
        @Test
        void post_single_null() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.post("/test", (Object) null);

            steps.verifyRequest(": [{path= '/test' body.rawBytes.base64.string= 'null'}]");
        }

        @SneakyThrows
        @Test
        void post_single_number() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.post("/test", 1);

            steps.verifyRequest(": [{path= '/test' body.rawBytes.base64.string= '1'}]");
        }

        @SneakyThrows
        @Test
        void post_map() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.post("/test", new HashMap<Object, Object>() {{
                put("key1", 1);
                put("key2", "str");
            }});

            steps.verifyRequest(": [{path= '/test' body.json= { key1= 1 key2= str}}]");
        }

        @SneakyThrows
        @Test
        void post_list() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.post("/test", asList(1, "hello", true));

            steps.verifyRequest(": [{path= '/test' body.json= [1 hello true]}]");
        }
    }

    @Nested
    class PutObject {

        @SneakyThrows
        @Test
        void post_single_value() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.put("/test", (Object) "hello");

            steps.verifyRequest(": [{path= '/test' body.rawBytes.base64.string= '\"hello\"'}]");
        }

        @SneakyThrows
        @Test
        void post_single_null() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.put("/test", (Object) null);

            steps.verifyRequest(": [{path= '/test' body.rawBytes.base64.string= 'null'}]");
        }

        @SneakyThrows
        @Test
        void post_single_number() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.put("/test", 1);

            steps.verifyRequest(": [{path= '/test' body.rawBytes.base64.string= '1'}]");
        }

        @SneakyThrows
        @Test
        void post_map() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.put("/test", new HashMap<Object, Object>() {{
                put("key1", 1);
                put("key2", "str");
            }});

            steps.verifyRequest(": [{path= '/test' body.json= { key1= 1 key2= str}}]");
        }

        @SneakyThrows
        @Test
        void post_list() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.put("/test", asList(1, "hello", true));

            steps.verifyRequest(": [{path= '/test' body.json= [1 hello true]}]");
        }
    }
}
