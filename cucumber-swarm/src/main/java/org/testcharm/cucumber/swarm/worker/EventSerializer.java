package org.testcharm.cucumber.swarm.worker;

import io.cucumber.plugin.event.TestCaseStarted;
import org.testcharm.cucumber.swarm.DataMapper;
import org.testcharm.message.MessageConverterRegistry;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class EventSerializer {
    public final DataMapper dataMapper;

    public EventSerializer(DataMapper dataMapper) {
        this.dataMapper = dataMapper;
    }

    public String serialize(Object event) {
        String type = event.getClass().getName();
        switch (type) {
            case "io.cucumber.plugin.event.TestCaseStarted":
                HashMap<String, Object> message = new LinkedHashMap<>();
                message.put("type", type);
                TestCaseStarted testCaseStarted = (TestCaseStarted) event;
                HashMap<String, Object> data = new LinkedHashMap<>();
                data.put("testCase", dataMapper.testCaseKey(testCaseStarted.getTestCase()));
                data.put("timeInstant", testCaseStarted.getInstant().toEpochMilli());
                message.put("data", data);
                return MessageConverterRegistry.jsonConverter().serialize(message);
            default:
                throw new IllegalArgumentException("Unsupported event type: " + type);
        }
    }
}
