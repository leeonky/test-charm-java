package org.testcharm.cucumber.swarm.worker;

import io.cucumber.plugin.event.*;
import org.testcharm.cucumber.swarm.DataMapper;
import org.testcharm.cucumber.swarm.ExceptionSerializer;
import org.testcharm.message.MessageConverterRegistry;

import java.util.LinkedHashMap;
import java.util.Map;

public class EventSerializer {
    public final DataMapper dataMapper;

    public EventSerializer(DataMapper dataMapper) {
        this.dataMapper = dataMapper;
    }

    public String serialize(Object event) {
        switch (event.getClass().getName()) {
            case "io.cucumber.plugin.event.TestCaseStarted":
                return eventBuilder((TestCaseStarted) event).build();
            case "io.cucumber.plugin.event.TestCaseFinished":
                TestCaseFinished testCaseFinished = (TestCaseFinished) event;
                return eventBuilder(testCaseFinished)
                        .setResult(testCaseFinished.getResult()).build();
            case "io.cucumber.plugin.event.TestStepStarted":
                TestStepStarted testStepStarted = (TestStepStarted) event;
                return eventBuilder(testStepStarted)
                        .setTestStep(testStepStarted.getTestCase(), testStepStarted.getTestStep()).build();
            case "io.cucumber.plugin.event.TestStepFinished":
                TestStepFinished testStepFinished = (TestStepFinished) event;
                return eventBuilder(testStepFinished)
                        .setTestStep(testStepFinished.getTestCase(), testStepFinished.getTestStep())
                        .setResult(testStepFinished.getResult()).build();
            default:
                throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
        }
    }

    EventBuilder eventBuilder(TestCaseEvent event) {
        return new EventBuilder(event);
    }

    class EventBuilder {
        private final Map<String, Object> content = new LinkedHashMap<>();
        private final Map<String, Object> data = new LinkedHashMap<>();

        EventBuilder(TestCaseEvent event) {
            content.put("type", event.getClass().getName());
            content.put("data", data);
            data.put("testCase", dataMapper.testCaseKey(event.getTestCase()));
            data.put("timeInstant", event.getInstant().toEpochMilli());
        }

        EventBuilder setTestStep(TestCase testCase, TestStep testStep) {
            data.put("testStep", testCase.getTestSteps().indexOf(testStep));
            return this;
        }

        EventBuilder setResult(Result result) {
            LinkedHashMap<String, Object> resultData = new LinkedHashMap<String, Object>() {{
                put("status", result.getStatus().name());
                put("duration", result.getDuration().toMillis());
            }};
            ExceptionSerializer.serialize(result.getError(), resultData, "error");
            data.put("result", resultData);
            return this;
        }

        String build() {
            return MessageConverterRegistry.jsonConverter().serialize(content);
        }
    }
}
