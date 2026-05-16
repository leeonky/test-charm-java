package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Step;
import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.event.*;
import org.testcharm.cucumber.swarm.ExceptionSerializer;
import org.testcharm.message.MessageConverterRegistry;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

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
            case "io.cucumber.messages.types.TestCase":
                TestCase testCase = eventParser.getTestCase();
                Pickle pickle = eventParser.getPickle();
                return Envelope.of(new io.cucumber.messages.types.TestCase(testCase.getId().toString(), pickle.getId(),
                        createTestSteps(testCase), eventParser.get("testRunStartedId")));
            default:
                throw new IllegalArgumentException("Unsupported event type: " + message.get("type"));
        }
    }

    private List<io.cucumber.messages.types.TestStep> createTestSteps(TestCase testCase) {
        return testCase.getTestSteps().stream().map(this::createTestStep).collect(toList());
    }

    private io.cucumber.messages.types.TestStep createTestStep(TestStep testStep) {
        if (testStep instanceof PickleStepTestStep) {
            PickleStepTestStep pickleStepTestStep = (PickleStepTestStep) testStep;
            return new io.cucumber.messages.types.TestStep(null, testStep.getId().toString(),
                    ((Step) pickleStepTestStep.getStep()).getId(), emptyList(), emptyList());
        }
        return new io.cucumber.messages.types.TestStep(null, testStep.getId().toString(), null, emptyList(), emptyList());
    }

    private class EventParser {
        private final Map<String, Object> data;

        private EventParser(Map<String, Object> message) {
            data = (Map<String, Object>) message.get("data");
        }

        private TestCase getTestCase() {
            return dataMapper.testCase((String) data.get("testCase"));
        }

        private Pickle getPickle() {
            return dataMapper.pickle((String) data.get("testCase"));
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

        public <T> T get(String key) {
            return (T) data.get(key);
        }
    }
}
