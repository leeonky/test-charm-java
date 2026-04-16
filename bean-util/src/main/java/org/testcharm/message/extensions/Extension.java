package org.testcharm.message.extensions;

import org.testcharm.message.Format;
import org.testcharm.message.JsonMessageConverter;
import org.testcharm.message.MessageConverterExtension;
import org.testcharm.message.MessageConverterRegistry;

public class Extension implements MessageConverterExtension {
    @Override
    public void extend(MessageConverterRegistry registry) {
        registry.register(Format.json(), new JsonMessageConverter());
    }
}
