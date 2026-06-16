package org.testcharm.dal.extensions;

import org.testcharm.dal.DAL;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.Extension;
import org.testcharm.dal.runtime.Order;
import org.testcharm.dal.runtime.inspector.Dumper;
import org.testcharm.dal.runtime.inspector.DumpingBuffer;

import java.lang.reflect.Type;
import java.time.*;
import java.util.Date;
import java.util.UUID;

import static org.testcharm.dal.runtime.Order.BUILD_IN;
import static org.testcharm.dal.runtime.inspector.Dumper.STRING_DUMPER;
import static org.testcharm.dal.runtime.inspector.Dumper.VALUE_DUMPER;

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
                .registerDumper(MetaShould.PredicateMethod.class, predicateMethodData ->
                        (data, dumpingBuffer) -> data.value().dump(dumpingBuffer))
        ;
    }

    @SuppressWarnings("unchecked")
    private void registerValueTypes(DAL dal, Class<?>... types) {
        for (Class<?> type : types)
            dal.getRuntimeContextBuilder().registerDumper(type, data -> (Dumper) VALUE_DUMPER);
    }

    private static class StackTraceDumper implements Dumper<StackTraceElement[]> {

        @Override
        public void dump(Data<StackTraceElement[]> data, DumpingBuffer dumpingBuffer) {
            DumpingBuffer sub = dumpingBuffer.indent();
            for (StackTraceElement stackTraceElement : data.value())
                sub.newLine().append("at " + stackTraceElement);
        }
    }
}
