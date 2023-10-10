package com.github.leeonky.cucumber.restful;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static java.util.Arrays.asList;

public class RestfulStepPatchTest {
    private RestfulStep restfulStep = new RestfulStep();
    private Steps steps = new Steps(restfulStep);

    @AfterEach
    void resetMockServer() {
        steps.stopMockServer();
    }

    @Nested
    class PatchObject {

        @SneakyThrows
        @Test
        void patch_single_value() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.patch("/test", (Object) "hello");

            steps.verifyRequest(": [{path= '/test' body.rawBytes.base64.string= '\"hello\"'}]");
        }

        @SneakyThrows
        @Test
        void patch_single_null() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.patch("/test", (Object) null);

            steps.verifyRequest(": [{path= '/test' body.rawBytes.base64.string= 'null'}]");
        }

        @SneakyThrows
        @Test
        void patch_single_number() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.patch("/test", 1);

            steps.verifyRequest(": [{path= '/test' body.rawBytes.base64.string= '1'}]");
        }

        @SneakyThrows
        @Test
        void patch_map() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.patch("/test", new HashMap<Object, Object>() {{
                put("key1", 1);
                put("key2", "str");
            }});

            steps.verifyRequest(": [{path= '/test' body.json= { key1= 1 key2= str}}]");
        }

        @SneakyThrows
        @Test
        void patch_list() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.patch("/test", asList(1, "hello", true));

            steps.verifyRequest(": [{path= '/test' body.json= [1 hello true]}]");
        }
    }

}
