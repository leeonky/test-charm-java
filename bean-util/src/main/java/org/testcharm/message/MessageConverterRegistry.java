package org.testcharm.message;

import java.util.HashMap;
import java.util.Map;

public class MessageConverterRegistry {
    private final Map<Format, MessageConverter> EMPTY_MAP = new HashMap<>();
    private final Map<Format, MessageConverter> defaultMessageConverters = new HashMap<>();
    private final Map<String, Map<Format, MessageConverter>> moduleMessageConverters = new HashMap<>();

    public MessageConverterRegistry register(String module, Format format, MessageConverter messageConverter) {
        moduleMessageConverters.computeIfAbsent(module, ig -> new HashMap<>()).put(format, messageConverter);
        return this;
    }

    public MessageConverterRegistry register(Format format, MessageConverter messageConverter) {
        defaultMessageConverters.put(format, messageConverter);
        return this;
    }

    public MessageConverter module(String module, Format format) {
        return moduleMessageConverters.getOrDefault(module, EMPTY_MAP).computeIfAbsent(format, ig -> {
            throw new IllegalArgumentException("No message converter for module: " + module + " and format: " + format);
        });
    }

    public MessageConverter moduleOrDefault(String module, Format format) {
        return moduleMessageConverters.getOrDefault(module, defaultMessageConverters).computeIfAbsent(format, ig -> {
            throw new IllegalArgumentException("No message converter for module: " + module + " and format: " + format);
        });
    }

    public MessageConverter module(Format format) {
        return defaultMessageConverters.computeIfAbsent(format, ig -> {
            throw new IllegalArgumentException("No default message converter for format: " + format);
        });
    }
}
