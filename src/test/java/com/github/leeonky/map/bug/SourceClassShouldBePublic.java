package com.github.leeonky.map.bug;

import com.github.leeonky.map.Mapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SourceClassShouldBePublic {

    @Test
    void source_class_should_be_public() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> new Mapper("com.github.leeonky.invalidmap.bug"));

        assertThat(runtimeException).hasMessage("com.github.leeonky.invalidmap.bug.PackagePrivateSource should be public");
    }
}
