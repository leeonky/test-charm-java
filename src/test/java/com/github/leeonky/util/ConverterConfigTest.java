package com.github.leeonky.util;

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

    public static class From {
    }

    public static class To {
    }
}
