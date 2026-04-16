package org.testcharm.message;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Format {
    private static final ConcurrentHashMap<String, Format> formats = new ConcurrentHashMap<>();

    private final String name;

    private Format(String name) {
        this.name = name;
    }

    synchronized public static Format format(String name) {
        return formats.computeIfAbsent(Objects.requireNonNull(name.toLowerCase()), Format::new);
    }

    public static Format json() {
        return format("json");
    }

    @Override
    public String toString() {
        return name;
    }
}
