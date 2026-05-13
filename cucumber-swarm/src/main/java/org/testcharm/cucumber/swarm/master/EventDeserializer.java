package org.testcharm.cucumber.swarm.master;

import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import org.testcharm.cucumber.swarm.ExceptionSerializer;
import org.testcharm.message.MessageConverterRegistry;

import java.time.Instant;
import java.util.Map;

@SuppressWarnings("unchecked")
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
                return getTestCaseStarted((Map<String, Object>) message.get("data"));
            case "io.cucumber.plugin.event.TestCaseFinished":
                return getTestCaseFinished((Map<String, Object>) message.get("data"));
            default:
                throw new IllegalArgumentException("Unsupported event type: " + type);
        }
    }

    private TestCaseStarted getTestCaseStarted(Map<String, Object> data) {
        String testCaseKey = (String) data.get("testCase");
        return new TestCaseStarted(Instant.ofEpochMilli(((Number) data.get("timeInstant")).longValue()),
                dataMapper.testCase(testCaseKey));
    }

    private TestCaseFinished getTestCaseFinished(Map<String, Object> data) {
        String testCaseKey = (String) data.get("testCase");
        Map<String, Object> result = (Map<String, Object>) data.get("result");
        return new TestCaseFinished(Instant.ofEpochMilli(((Number) data.get("timeInstant")).longValue()),
                dataMapper.testCase(testCaseKey), new Result(Status.valueOf(result.get("status").toString()),
                java.time.Duration.ofMillis(((Number) result.get("duration")).longValue()),
                ExceptionSerializer.deserialize(result, "error")));
    }
}
