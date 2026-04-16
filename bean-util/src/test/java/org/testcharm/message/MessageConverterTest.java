package org.testcharm.message;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        class Serialize {

//            @Test
//            void serialize_null() {
//                assertEquals("null", messageConverter.serialize(null));
//            }

        }
    }
}
