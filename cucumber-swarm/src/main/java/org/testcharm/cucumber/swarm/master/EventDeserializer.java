package org.testcharm.cucumber.swarm.master;

import io.cucumber.plugin.event.TestCaseStarted;
import org.testcharm.message.MessageConverterRegistry;

import java.time.Instant;
import java.util.Map;

public class EventDeserializer {
    public final MasterDataMapper dataMapper;

    public EventDeserializer(MasterDataMapper dataMapper) {
        this.dataMapper = dataMapper;
    }

    public Object deserialize(String json) {
        Map<String, Object> message = (Map<String, Object>) MessageConverterRegistry.jsonConverter().deserialize(json);
        String type = (String) message.get("type");
        switch (type) {
            case "io.cucumber.plugin.event.TestCaseStarted":
                Map<String, Object> data = (Map<String, Object>) message.get("data");
                String testCaseKey = (String) data.get("testCase");
                return new TestCaseStarted(Instant.ofEpochMilli((Long) data.get("timeInstant")),
                        dataMapper.testCase(testCaseKey));
            default:
                throw new IllegalArgumentException("Unsupported event type: " + type);
        }
    }
}
