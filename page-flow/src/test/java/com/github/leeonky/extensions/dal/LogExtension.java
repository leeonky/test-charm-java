package com.github.leeonky.extensions.dal;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.valfirst.slf4jtest.LoggingEvent;

public class LogExtension implements Extension {
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerDumper(LoggingEvent.class, loggingEventData -> (data, dumpingBuffer) ->
                dumpingBuffer.append(data.value().toString()));
    }
}
