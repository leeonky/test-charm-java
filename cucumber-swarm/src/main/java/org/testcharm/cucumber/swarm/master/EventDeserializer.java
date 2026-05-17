package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Step;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.StepMatchArgument;
import io.cucumber.messages.types.StepMatchArgumentsList;
import io.cucumber.plugin.event.*;
import org.testcharm.cucumber.swarm.ExceptionSerializer;
import org.testcharm.message.MessageConverterRegistry;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.testcharm.util.Zipped.zip;

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
                        eventParser.createTestSteps(testCase), eventParser.get("testRunStartedId")));
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

        public List<io.cucumber.messages.types.TestStep> createTestSteps(TestCase testCase) {
            return zip(testCase.getTestSteps(), this.<List<Map<String, Object>>>get("testSteps"))
                    .stream().map(zippedEntry -> {
                        List<String> stepDefinitionIds = (List<String>) zippedEntry.right().get("stepDefinitionIds");
                        if (stepDefinitionIds != null)
                            stepDefinitionIds = stepDefinitionIds.stream().map(key -> dataMapper.stepDefinition(key).getId()).collect(toList());

                        List<StepMatchArgumentsList> stepMatchArgumentsLists = null;
                        List<Map<String, List<Map<String, Object>>>> stepMatchArgumentsListsData =
                                (List<Map<String, List<Map<String, Object>>>>) zippedEntry.right().get("stepMatchArgumentsLists");
                        if (stepMatchArgumentsListsData != null) {
                            stepMatchArgumentsLists = stepMatchArgumentsListsData.stream().map(stepMatchArgumentsList -> {
                                List<StepMatchArgument> collect = stepMatchArgumentsList.get("stepMatchArguments").stream().map(stepMatchArgument -> {
                                    return new StepMatchArgument(deserializeGroup((Map<String, Object>) stepMatchArgument.get("group")),
                                            (String) stepMatchArgument.get("parameterTypeName"));
                                }).collect(toList());
                                return new StepMatchArgumentsList(collect);
                            }).collect(toList());
                        }
                        String id = zippedEntry.left().getId().toString();
                        String pickleStepId = null, hookId = null;

                        if (zippedEntry.left() instanceof PickleStepTestStep) {
                            PickleStepTestStep pickleStepTestStep = (PickleStepTestStep) zippedEntry.left();
                            pickleStepId = ((Step) pickleStepTestStep.getStep()).getId();
                        } else if (zippedEntry.left() instanceof HookTestStep) {

                        }
                        return new io.cucumber.messages.types.TestStep(hookId, id, pickleStepId, stepDefinitionIds,
                                stepMatchArgumentsLists);
                    }).collect(toList());
        }

        private io.cucumber.messages.types.Group deserializeGroup(Map<String, Object> group) {
            return new io.cucumber.messages.types.Group(
                    ((List<Map<String, Object>>) group.get("children")).stream().map(this::deserializeGroup).collect(toList()),
                    ((Number) group.get("start")).longValue(), (String) group.get("value"));
        }
    }
}
