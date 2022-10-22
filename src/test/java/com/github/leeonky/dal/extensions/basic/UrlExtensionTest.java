package com.github.leeonky.dal.extensions.basic;

import com.github.leeonky.dal.DAL;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlExtensionTest {

    @SneakyThrows
    @Test
    void string_to_url() {
        DAL dal = DAL.getInstance();

        assertThat((Object) dal.evaluate(null, "'http://www.baidu.com'.url")).isInstanceOf(URL.class);
    }
}
