package org.testcharm.cucumber.swarm.master;

import io.cucumber.plugin.event.*;
import org.testcharm.cucumber.swarm.ExceptionSerializer;
import org.testcharm.message.MessageConverterRegistry;

import java.time.Duration;
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
        EventParser eventParser = new EventParser(message);
        switch ((String) message.get("type")) {
            case "io.cucumber.plugin.event.TestCaseStarted":
                return new TestCaseStarted(eventParser.getInstant(), eventParser.getTestCase());
            case "io.cucumber.plugin.event.TestCaseFinished":
                return new TestCaseFinished(eventParser.getInstant(), eventParser.getTestCase(), eventParser.getResult());
            case "io.cucumber.plugin.event.TestStepStarted":
                return new TestStepStarted(eventParser.getInstant(), eventParser.getTestCase(), eventParser.getTestStep());
            case "io.cucumber.plugin.event.TestStepFinished":
                return new TestStepFinished(eventParser.getInstant(), eventParser.getTestCase(), eventParser.getTestStep(),
                        eventParser.getResult());
            default:
                throw new IllegalArgumentException("Unsupported event type: " + message.get("type"));
        }
    }

    private class EventParser {
        private final Map<String, Object> data;

        private EventParser(Map<String, Object> message) {
            data = (Map<String, Object>) message.get("data");
        }

        private TestCase getTestCase() {
            return dataMapper.testCase((String) data.get("testCase"));
        }

        private Instant getInstant() {
            return Instant.ofEpochMilli(((Number) data.get("timeInstant")).longValue());
        }

        private Result getResult() {
            Map<String, Object> resultData = (Map<String, Object>) data.get("result");
            return new Result(Status.valueOf(resultData.get("status").toString()),
                    Duration.ofMillis(((Number) resultData.get("duration")).longValue()),
                    ExceptionSerializer.deserialize(resultData, "error"));
        }

        public TestStep getTestStep() {
            return getTestCase().getTestSteps().get(((Number) data.get("testStep")).intValue());
        }
    }
}
