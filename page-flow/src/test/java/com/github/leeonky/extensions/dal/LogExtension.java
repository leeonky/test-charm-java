package com.github.leeonky.extensions.dal;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumperFactory;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;
import com.github.valfirst.slf4jtest.LoggingEvent;

public class LogExtension implements Extension {
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerDumper(LoggingEvent.class, new DumperFactory<LoggingEvent>() {
            @Override
            public Dumper<LoggingEvent> apply(Data<LoggingEvent> loggingEventData) {
                return new Dumper<LoggingEvent>() {
                    @Override
                    public void dump(Data<LoggingEvent> data, DumpingBuffer dumpingBuffer) {
                        dumpingBuffer.append(data.value().toString());
                    }
                };
            }
        });
    }
}
