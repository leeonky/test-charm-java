package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Data.Resolved;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.Order;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

import java.lang.reflect.Type;
import java.time.*;
import java.util.Date;
import java.util.UUID;

import static com.github.leeonky.dal.runtime.Order.BUILD_IN;
import static com.github.leeonky.dal.runtime.inspector.Dumper.STRING_DUMPER;
import static com.github.leeonky.dal.runtime.inspector.Dumper.VALUE_DUMPER;

@Order(BUILD_IN)
@SuppressWarnings("used")
public class Dumpers implements Extension {
    private static final StackTraceDumper STACK_TRACE_DUMPER = new StackTraceDumper();

    @Override
    public void extend(DAL dal) {
        registerValueTypes(dal, Type.class, Number.class, Boolean.class, UUID.class, Instant.class, Date.class,
                LocalTime.class, LocalDate.class, LocalDateTime.class, OffsetDateTime.class, ZonedDateTime.class,
                YearMonth.class);
        dal.getRuntimeContextBuilder()
                .registerDumper(CharSequence.class, data -> STRING_DUMPER)
                .registerDumper(StackTraceElement[].class, data -> STACK_TRACE_DUMPER)
        ;
    }

    private void registerValueTypes(DAL dal, Class<?>... types) {
        for (Class<?> type : types)
            dal.getRuntimeContextBuilder().registerDumper(type, data -> VALUE_DUMPER);
    }

    private static class StackTraceDumper implements Dumper {

        @Override
        public void dump(Resolved data, DumpingBuffer dumpingBuffer) {
            DumpingBuffer sub = dumpingBuffer.indent();
            data.list().forEach(s -> sub.newLine().append("at " + s.value().toString()));
        }
    }
}
