package com.github.leeonky.extensions.dal;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumperFactory;
import com.github.leeonky.dal.runtime.inspector.KeyValueDumper;
import com.github.leeonky.pf.Element;
import com.github.valfirst.slf4jtest.LoggingEvent;

import java.util.Set;
import java.util.TreeSet;

public class LogExtension implements Extension {
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerDumper(LoggingEvent.class, loggingEventData -> (data, dumpingBuffer) ->
                dumpingBuffer.append(data.value().toString()));

        dal.getRuntimeContextBuilder().registerDumper(Element.class, new DumperFactory<Element>() {
            @Override
            public Dumper<Element> apply(Data<Element> elementData) {
                return new KeyValueDumper<Element>() {
                    @Override
                    protected Set<?> getFieldNames(Data<Element> data) {
                        return new TreeSet<>(super.getFieldNames(data));
                    }
                };
            }
        });
    }
}
