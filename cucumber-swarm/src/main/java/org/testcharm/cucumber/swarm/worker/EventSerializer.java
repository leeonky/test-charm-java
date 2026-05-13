package org.testcharm.cucumber.swarm.worker;

import io.cucumber.plugin.event.TestCaseFinished;
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
        HashMap<String, Object> message = new LinkedHashMap<>();
        HashMap<String, Object> data = new LinkedHashMap<>();
        String type = event.getClass().getName();
        switch (type) {
            case "io.cucumber.plugin.event.TestCaseStarted":
                message.put("type", type);
                TestCaseStarted testCaseStarted = (TestCaseStarted) event;
                data.put("testCase", dataMapper.testCaseKey(testCaseStarted.getTestCase()));
                data.put("timeInstant", testCaseStarted.getInstant().toEpochMilli());
                message.put("data", data);
                return MessageConverterRegistry.jsonConverter().serialize(message);
            case "io.cucumber.plugin.event.TestCaseFinished":
                message.put("type", type);
                TestCaseFinished testCaseFinished = (TestCaseFinished) event;
                data.put("testCase", dataMapper.testCaseKey(testCaseFinished.getTestCase()));
                data.put("timeInstant", testCaseFinished.getInstant().toEpochMilli());
                data.put("result", new HashMap<String, Object>() {{
                    put("status", testCaseFinished.getResult().getStatus().name());
                    put("duration", testCaseFinished.getResult().getDuration().toMillis());
                }});
                message.put("data", data);
                return MessageConverterRegistry.jsonConverter().serialize(message);
            default:
                throw new IllegalArgumentException("Unsupported event type: " + type);
        }
    }
}
