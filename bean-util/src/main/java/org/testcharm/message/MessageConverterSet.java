package org.testcharm.message;

import java.util.HashMap;
import java.util.Map;

public class MessageConverterSet {
    private final Map<Format, MessageConverter> defaultMessageConverters = new HashMap<>();
    private final Map<String, Map<Format, MessageConverter>> moduleMessageConverters = new HashMap<>();

    public MessageConverterSet register(String module, Format format, MessageConverter messageConverter) {
        moduleMessageConverters.computeIfAbsent(module, ig -> new HashMap<>()).put(format, messageConverter);
        return this;
    }

    public MessageConverter messageConverter(String module, Format format) {
        return moduleMessageConverters.getOrDefault(module, defaultMessageConverters).computeIfAbsent(format, ig -> {
            throw new IllegalArgumentException("No message converter for module: " + module + " and format: " + format);
        });
    }

    public MessageConverterSet register(Format format, MessageConverter messageConverter) {
        defaultMessageConverters.put(format, messageConverter);
        return this;
    }

    public MessageConverter messageConverter(Format format) {
        return defaultMessageConverters.computeIfAbsent(format, ig -> {
            throw new IllegalArgumentException("No default message converter for format: " + format);
        });
    }
}
