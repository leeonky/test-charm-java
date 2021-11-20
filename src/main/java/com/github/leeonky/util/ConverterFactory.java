package com.github.leeonky.util;

import java.util.Iterator;
import java.util.ServiceLoader;

public interface ConverterFactory {
    static Converter create() {
        Iterator<ConverterFactory> iterator = ServiceLoader.load(ConverterFactory.class).iterator();
        if (iterator.hasNext())
            return iterator.next().newInstance();
        return Converter.createDefault();
    }

    Converter newInstance();
}
