package org.testcharm.cucumber.restful;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.jfactory.JFactory;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.testcharm.dal.Assertions.expect;

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

            restfulStep.postObjectInDefault("/test", "hello");

            steps.verifyRequest(": [{path= '/test' body.rawBytes.base64.string= '\"hello\"'}]");
        }

        @SneakyThrows
        @Test
        void post_single_null() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.postObjectInDefault("/test", null);

            steps.verifyRequest(": [{path= '/test' body.rawBytes.base64.string= 'null'}]");
        }

        @SneakyThrows
        @Test
        void post_single_number() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.postObjectInDefault("/test", 1);

            steps.verifyRequest(": [{path= '/test' body.rawBytes.base64.string= '1'}]");
        }

        @SneakyThrows
        @Test
        void post_map() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.postObjectInDefault("/test", new HashMap<Object, Object>() {{
                put("key1", 1);
                put("key2", "str");
            }});

            steps.verifyRequest(": [{path= '/test' body.json= { key1= 1 key2= str}}]");
        }

        @SneakyThrows
        @Test
        void post_list() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.postObjectInDefault("/test", asList(1, "hello", true));

            steps.verifyRequest(": [{path= '/test' body.json= [1 hello true]}]");
        }
    }

    @Nested
    class PutObject {

        @SneakyThrows
        @Test
        void put_single_value() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.putObjectInDefault("/test", "hello");

            steps.verifyRequest(": [{path= '/test' body.rawBytes.base64.string= '\"hello\"'}]");
        }

        @SneakyThrows
        @Test
        void put_single_null() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.putObjectInDefault("/test", null);

            steps.verifyRequest(": [{path= '/test' body.rawBytes.base64.string= 'null'}]");
        }

        @SneakyThrows
        @Test
        void put_single_number() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.putObjectInDefault("/test", 1);

            steps.verifyRequest(": [{path= '/test' body.rawBytes.base64.string= '1'}]");
        }

        @SneakyThrows
        @Test
        void put_map() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.putObjectInDefault("/test", new HashMap<Object, Object>() {{
                put("key1", 1);
                put("key2", "str");
            }});

            steps.verifyRequest(": [{path= '/test' body.json= { key1= 1 key2= str}}]");
        }

        @SneakyThrows
        @Test
        void put_list() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.putObjectInDefault("/test", asList(1, "hello", true));

            steps.verifyRequest(": [{path= '/test' body.json= [1 hello true]}]");
        }
    }

    @Nested
    class GetFromResponse {

        @SneakyThrows
        @Test
        void get_response_property() {
            restfulStep.setBaseUrl("http://www.a.com:8080");

            restfulStep.post("/test", "text/plain", "any-string");

            assertThat((Object) restfulStep.response("code")).isEqualTo(404);
        }
    }

    @Nested
    class InvalidPostOctetStream {

        @SneakyThrows
        @Test
        void unsupported_data_type() {
            restfulStep.setJFactory(new JFactory());
            restfulStep.setBaseUrl("http://www.a.com:8080");
            assertThrows(IllegalArgumentException.class, () ->
                    restfulStep.post("/test", "dal:application/octet-stream", "100"));
        }
    }

    @Nested
    class Response {

        @SneakyThrows
        @Test
        void status_ok_with_input_stream() {
            HttpURLConnection connection = mock(HttpURLConnection.class);

            when(connection.getResponseCode()).thenReturn(200);
            when(connection.getInputStream()).thenReturn(new ByteArrayInputStream("hello".getBytes()));

            RestfulStep.Response response = new RestfulStep.Response(connection);

            expect(response).should(": {code= 200, body.string= hello}");
        }

        @SneakyThrows
        @Test
        void status_not_ok_with_input_stream() {
            HttpURLConnection connection = mock(HttpURLConnection.class);

            when(connection.getResponseCode()).thenReturn(400);
            when(connection.getErrorStream()).thenReturn(new ByteArrayInputStream("error".getBytes()));

            RestfulStep.Response response = new RestfulStep.Response(connection);

            expect(response).should(": {code= 400, body.string= error}");
        }

        @Nested
        class Headers {
            HttpURLConnection connection = mock(HttpURLConnection.class);

            @BeforeEach
            @SneakyThrows
            void setup() {
                when(connection.getResponseCode()).thenReturn(200);
                when(connection.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
            }

            @Test
            void get_header_single_value() {
                Map<String, List<String>> headers = new HashMap<String, List<String>>() {{
                    put("Content-Type", Collections.singletonList("application/json"));
                }};

                when(connection.getHeaderFields()).thenReturn(headers);

                RestfulStep.Response response = new RestfulStep.Response(connection);

                expect(response.getHeaders()).should(": {Content-Type= application/json}");
            }

            @Test
            void get_header_multiple_values() {
                Map<String, List<String>> headers = new HashMap<String, List<String>>() {{
                    put("key", asList("value1", "value2"));
                }};

                when(connection.getHeaderFields()).thenReturn(headers);

                RestfulStep.Response response = new RestfulStep.Response(connection);

                expect(response.getHeaders()).should(": {key= [value1 value2]}");
            }

            @Test
            void get_header_null_value() {
                Map<String, List<String>> headers = new HashMap<String, List<String>>() {{
                    put("key", null);
                }};

                when(connection.getHeaderFields()).thenReturn(headers);

                RestfulStep.Response response = new RestfulStep.Response(connection);

                expect(response.getHeaders()).should("= {}");
            }

            @Test
            void get_header_null_key() {
                Map<String, List<String>> headers = new HashMap<String, List<String>>() {{
                    put(null, Collections.singletonList("value"));
                }};

                when(connection.getHeaderFields()).thenReturn(headers);

                RestfulStep.Response response = new RestfulStep.Response(connection);

                expect(response.getHeaders()).should("= {}");
            }

            @Test
            void should_cache_headers() {
                Map<String, List<String>> headers = new HashMap<String, List<String>>() {{
                    put("key", Collections.singletonList("value"));
                }};

                when(connection.getHeaderFields()).thenReturn(headers);

                RestfulStep.Response response = new RestfulStep.Response(connection);

                response.getHeaders();
                response.getHeaders();

                verify(connection, timeout(1)).getHeaderFields();
            }

            @Test
            void parse_file_name() {
                when(connection.getHeaderField("Content-Disposition")).thenReturn("attachment; filename=\"test file.txt\"");

                RestfulStep.Response response = new RestfulStep.Response(connection);

                expect(response.fileName()).isEqualTo("test file.txt");
            }

            @Test
            void should_return_all_header_value_when_invalid_disposition_format() {
                when(connection.getHeaderField("Content-Disposition")).thenReturn("attachment; file=invalid");

                RestfulStep.Response response = new RestfulStep.Response(connection);

                expect(response.fileName()).isEqualTo("attachment; file=invalid");
            }
        }
    }
}
