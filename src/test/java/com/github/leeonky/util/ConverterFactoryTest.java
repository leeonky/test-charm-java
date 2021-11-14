package com.github.leeonky.util;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ServiceLoader;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConverterFactoryTest {

    @Test
    void create_Converter_from_Converter_factory() {
        Converter instance = new Converter();
        ConverterFactory mockConverterFactory = () -> instance;

        Converter actual;
        try (MockedStatic<ServiceLoader> utilities = Mockito.mockStatic(ServiceLoader.class)) {
            ServiceLoader mockServiceLoader = mock(ServiceLoader.class);
            when(mockServiceLoader.iterator()).thenReturn(singletonList(mockConverterFactory).iterator());
            utilities.when(() -> ServiceLoader.load(ConverterFactory.class)).thenReturn(mockServiceLoader);
            actual = Converter.create();
        }

        assertThat(actual).isEqualTo(instance);
    }

    @Test
    void create_Converter_by_new_when_no_Converter_factory() {
        assertThat(Converter.create()).isInstanceOf(Converter.class);
    }
}