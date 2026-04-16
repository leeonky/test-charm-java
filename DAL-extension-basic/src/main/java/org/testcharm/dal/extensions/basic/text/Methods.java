package org.testcharm.dal.extensions.basic.text;

import org.testcharm.message.Format;
import org.testcharm.message.MessageConverter;
import org.testcharm.message.MessageConverterRegistry;
import org.yaml.snakeyaml.Yaml;

public class Methods {
    private static final MessageConverter jsonConverter = MessageConverterRegistry.messageConverterRegistry().format(Format.json());

    public static Object json(byte[] data) {
        return json(new String(data));
    }

    public static Object json(CharSequence data) {
        return jsonConverter.deserialize(data.toString());
    }

    public static Object yaml(byte[] data) {
        return yaml(new String(data));
    }

    public static Object yaml(CharSequence data) {
        return new Yaml().load(data.toString());
    }
}
