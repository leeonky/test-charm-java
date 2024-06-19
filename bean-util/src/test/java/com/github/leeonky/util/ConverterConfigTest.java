package com.github.leeonky.util;

import com.github.leeonky.extensions.util.TestConverterExtension2;
import com.github.leeonky.util.extensions.TestConverterExtension;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConverterConfigTest {

    @Test
    void support_extend_converter() {
        TestConverterExtension.extender = c -> c.addTypeConverter(From.class, To.class, from -> new To());

        assertThat(new Converter().extend().convert(To.class, new From()))
                .isInstanceOf(To.class);
    }

    @Test
    void support_extend_converter_in_new_another_package() {
        TestConverterExtension2.extender = c -> c.addTypeConverter(From2.class, To2.class, from -> new To2());

        assertThat(new Converter().extend().convert(To2.class, new From2()))
                .isInstanceOf(To2.class);
    }

    public static class From {
    }

    public static class To {
    }

    public static class From2 {
    }

    public static class To2 {
    }
}
