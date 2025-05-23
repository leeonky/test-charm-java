package com.github.leeonky.dal.runtime.inspector;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DumpingBufferTest {

    public static class Bean {
    }

    public static class BeanDumper<T> implements Dumper<T> {
        @Override
        public void dump(Data<T> data, DumpingBuffer dumpingBuffer) {
            throw new RuntimeException("dump error");
        }
    }

    @Test
    void ignore_dump_error() {
        RuntimeContextBuilder builder = new RuntimeContextBuilder();
        builder.registerDumper(Bean.class, d -> new BeanDumper<>());
        RuntimeContextBuilder.DALRuntimeContext context = builder.build(new Bean());

        assertThat(context.getThis().dumpValue()).isEqualTo("*throw* java.lang.RuntimeException: dump error");
        assertThat(context.getThis().dump()).isEqualTo("*throw* java.lang.RuntimeException: dump error");
    }
}