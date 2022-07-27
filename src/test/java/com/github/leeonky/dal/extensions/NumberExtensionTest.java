package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class NumberExtensionTest {
    private final DAL dal = DAL.getInstance();

    @SneakyThrows
    @Test
    void string_to_number() {
        assertThat((Object) dal.evaluate("1", ".number")).isEqualTo(1);
        assertThat((Object) dal.evaluate("1", ".byte")).isEqualTo((byte) 1);
        assertThat((Object) dal.evaluate("1", ".short")).isEqualTo((short) 1);
        assertThat((Object) dal.evaluate("1", ".int")).isEqualTo(1);
        assertThat((Object) dal.evaluate("1", ".long")).isEqualTo(1L);
        assertThat((Object) dal.evaluate("1", ".float")).isEqualTo(1.0F);
        assertThat((Object) dal.evaluate("1", ".double")).isEqualTo(1.0D);
        assertThat((Object) dal.evaluate("1", ".bigInt")).isEqualTo(BigInteger.valueOf(1));
        assertThat((Object) dal.evaluate("1", ".decimal")).isEqualTo(BigDecimal.valueOf(1));
    }
}
