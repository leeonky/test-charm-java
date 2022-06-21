package com.github.leeonky.extensions.util;

import com.github.leeonky.util.Converter;
import com.github.leeonky.util.ConverterExtension;

import java.util.function.Consumer;

public class TestConverterExtension2 implements ConverterExtension {
    public static Consumer<Converter> extender = c -> {
    };

    @Override
    public void extend(Converter converter) {
        extender.accept(converter);
    }
}
