package org.testcharm.cucumber.swarm.worker;

import io.cucumber.plugin.event.*;
import org.testcharm.cucumber.swarm.ExceptionSerializer;
import org.testcharm.message.MessageConverterRegistry;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class EventSerializer {
    public final WorkerDataMapper dataMapper;

    public EventSerializer(WorkerDataMapper dataMapper) {
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
            case "io.cucumber.messages.types.TestCase": {
                io.cucumber.messages.types.TestCase testCase = (io.cucumber.messages.types.TestCase) event;
                return envelopBuilder(event.getClass().getName())
                        .setTestCase(testCase)
                        .put("testRunStartedId", testCase.getTestRunStartedId().orElse(null)).build();
            }

            case "io.cucumber.messages.types.TestCaseStarted":
                io.cucumber.messages.types.TestCaseStarted testCaseStarted = (io.cucumber.messages.types.TestCaseStarted) event;
                return envelopBuilder(event.getClass().getName())
                        .put("attempt", testCaseStarted.getAttempt())
                        .put("id", testCaseStarted.getId())
                        .put("testCaseId", dataMapper.transformTestCaseIdToKey(testCaseStarted.getTestCaseId()))
                        .put("workerId", testCaseStarted.getWorkerId().orElse(null))
                        .put("timestamp", new HashMap<String, Long>() {{
                            put("seconds", testCaseStarted.getTimestamp().getSeconds());
                            put("nanos", testCaseStarted.getTimestamp().getNanos());
                        }}).build();
            case "io.cucumber.messages.types.TestStepStarted": {
                io.cucumber.messages.types.TestStepStarted testStepStartedMessage = (io.cucumber.messages.types.TestStepStarted) event;
                io.cucumber.messages.types.TestCase testCase = dataMapper.testCaseByStepId(testStepStartedMessage.getTestStepId());
                List<String> stepIds = testCase.getTestSteps().stream().map(io.cucumber.messages.types.TestStep::getId).collect(toList());
                return envelopBuilder(event.getClass().getName())
                        .put("testCaseStartedId", testStepStartedMessage.getTestCaseStartedId())
                        .put("testCase", dataMapper.pickleKey(dataMapper.pickleById(testCase.getPickleId())))
                        .put("testStepId", stepIds.indexOf(testStepStartedMessage.getTestStepId()))
                        .put("timestamp", new HashMap<String, Long>() {{
                            put("seconds", testStepStartedMessage.getTimestamp().getSeconds());
                            put("nanos", testStepStartedMessage.getTimestamp().getNanos());
                        }}).build();
            }
            default:
                throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
        }
    }

    EventBuilder eventBuilder(TestCaseEvent event) {
        return new EventBuilder(event);
    }

    class EventBuilder extends Builder {
        EventBuilder(TestCaseEvent event) {
            super(event.getClass().getName());
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

    }

    EnvelopBuilder envelopBuilder(String type) {
        return new EnvelopBuilder(type);
    }

    class EnvelopBuilder extends Builder {
        EnvelopBuilder(String type) {
            super(type);
        }

        EnvelopBuilder setTestCase(io.cucumber.messages.types.TestCase testCase) {
            data.put("testCase", dataMapper.pickleKey(dataMapper.pickleById(testCase.getPickleId())));
            data.put("testSteps", testCase.getTestSteps().stream().map(this::serializeTestStep).collect(toList()));
            return this;
        }

        EnvelopBuilder put(String key, Object value) {
            data.put(key, value);
            return this;
        }

        private Map<String, Object> serializeTestStep(io.cucumber.messages.types.TestStep testStep) {
            return new LinkedHashMap<String, Object>() {{
                put("hookId", testStep.getHookId().map(dataMapper::transformHookIdToKey).orElse(null));
                put("stepDefinitionIds", testStep.getStepDefinitionIds().map(ids ->
                        ids.stream().map(dataMapper::transformStepDefinitionIdToKey).collect(toList())).orElse(null));

                put("stepMatchArgumentsLists", testStep.getStepMatchArgumentsLists().map(stepMatchArgumentsLists -> {
                    return stepMatchArgumentsLists.stream().map(argList -> {
                        return new LinkedHashMap<String, Object>() {{
                            put("stepMatchArguments", argList.getStepMatchArguments().stream().map(arg -> new LinkedHashMap<String, Object>() {{
                                put("group", serializeGroup(arg.getGroup(), new LinkedHashMap<>()));
                                put("parameterTypeName", arg.getParameterTypeName().orElse(null));
                            }}).collect(toList()));
                        }};
                    }).collect(toList());
                }).orElse(null));
            }};
        }

        private Map<String, Object> serializeGroup(io.cucumber.messages.types.Group group, Map<String, Object> data) {
            data.put("children", group.getChildren().stream()
                    .map(child -> serializeGroup(child, new LinkedHashMap<>()))
                    .collect(toList()));
            data.put("start", group.getStart().orElse(null));
            data.put("value", group.getValue().orElse(null));
            return data;
        }
    }

    static class Builder {
        protected final Map<String, Object> content = new LinkedHashMap<>();
        protected final Map<String, Object> data = new LinkedHashMap<>();

        public Builder(String type) {
            content.put("type", type);
            content.put("data", data);
        }

        String build() {
            return MessageConverterRegistry.jsonConverter().serialize(content);
        }
    }
}
