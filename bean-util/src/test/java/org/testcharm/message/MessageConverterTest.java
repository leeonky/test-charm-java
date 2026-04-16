package org.testcharm.message;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.testcharm.dal.Assertions.expectRun;
import static org.testcharm.message.Format.json;

class MessageConverterTest {

    @Nested
    class Register {
        @Test
        void register_default_by_module() {
            MessageConverterSet messageConverterSet = new MessageConverterSet();
            MessageConverter messageConverter = mock(MessageConverter.class);

            messageConverterSet.register(json(), messageConverter);

            assertEquals(messageConverter, messageConverterSet.messageConverter(json()));
        }

        @Test
        void raise_error_when_no_default() {
            MessageConverterSet messageConverterSet = new MessageConverterSet();

            expectRun(() -> messageConverterSet.messageConverter(json()))
                    .should("::throw.message= 'No default message converter for format: json'");
        }

        @Test
        void register_by_module() {
            MessageConverterSet messageConverterSet = new MessageConverterSet();
            MessageConverter messageConverter = mock(MessageConverter.class);

            messageConverterSet.register("target", json(), messageConverter);

            assertEquals(messageConverter, messageConverterSet.messageConverter("target", json()));
        }

        @Test
        void raise_error_when_no_module() {
            MessageConverterSet messageConverterSet = new MessageConverterSet();
            messageConverterSet.register("not-target", json(), mock(MessageConverter.class));
            messageConverterSet.register("target", Format.format("not-json"), mock(MessageConverter.class));

            expectRun(() -> messageConverterSet.messageConverter("target", json()))
                    .should("::throw.message= 'No message converter for module: target and format: json'");
        }

        @Test
        void fallback_to_default() {
            MessageConverterSet messageConverterSet = new MessageConverterSet();
            MessageConverter messageConverter = mock(MessageConverter.class);

            messageConverterSet.register(json(), messageConverter);

            assertEquals(messageConverter, messageConverterSet.messageConverter("not-exist", json()));
        }
    }

    @Nested
    class BuildInJsonMessageConverterTest {
        JsonMessageConverter messageConverter = new JsonMessageConverter();

        @Nested
        class Deserialize {

            @Test
            void deserialize_null() {
                assertNull(messageConverter.deserialize("null"));
            }

            @Test
            void deserialize_string() {
                assertEquals("string", messageConverter.deserialize("\"string\""));
            }

            @Test
            void deserialize_number() {
                assertEquals(123, messageConverter.deserialize("123"));
            }

            @Test
            void deserialize_boolean() {
                assertEquals(true, messageConverter.deserialize("true"));
            }

            @Test
            void deserialize_array() {
                assertEquals(java.util.Arrays.asList(1, 2, 3), messageConverter.deserialize("[1, 2, 3]"));
            }

            @Test
            void deserialize_object() {
                assertEquals(new HashMap<String, Object>() {{
                    put("key", "value");
                }}, messageConverter.deserialize("{\"key\": \"value\"}"));
            }

            @Test
            void deserialize_nested() {
                assertEquals(new HashMap<String, Object>() {{
                    put("key", java.util.Arrays.asList(1, 2, 3));
                }}, messageConverter.deserialize("{\"key\": [1, 2, 3]}"));
            }
        }

        @Nested
        class Serialize {

            @Nested
            class Primitive {

                @Test
                void serialize_null() {
                    assertEquals("null", messageConverter.serialize(null));
                }

                @Test
                void serialize_string() {
                    assertEquals("\"string\"", messageConverter.serialize("string"));
                }

                @Test
                void serialize_number() {
                    assertEquals("123", messageConverter.serialize(123));
                }

                @Test
                void serialize_boolean() {
                    assertEquals("true", messageConverter.serialize(true));
                }

                @Test
                void serialize_false_boolean() {
                    assertEquals("false", messageConverter.serialize(false));
                }


                public class Bean {
                    public int i = 5;
                    public String str = "hello";
                    public boolean f = true;
                }

                @Test
                void serialize_pojo() {
                    assertEquals("{\"str\":\"hello\",\"i\":5,\"f\":true}", messageConverter.serialize(new Bean()));
                }

                @Test
                void serialize_nested_pojo() {
                    assertEquals("{\"bean\":{\"str\":\"hello\",\"i\":5,\"f\":true}}", messageConverter.serialize(new HashMap<String, Object>() {{
                        put("bean", new Bean());
                    }}));
                }

                @Test
                void serialize_array() {
                    assertEquals("[1,2,3]", messageConverter.serialize(new int[]{1, 2, 3}));
                }

                @Test
                void serialize_nested_array() {
                    assertEquals("[{\"str\":\"hello\",\"i\":5,\"f\":true}]", messageConverter.serialize(new Object[]{new Bean()}));
                }
            }
        }
    }
}
