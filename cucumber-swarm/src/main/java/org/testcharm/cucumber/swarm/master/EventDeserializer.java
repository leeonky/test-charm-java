package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.gherkin.Step;
import io.cucumber.messages.types.Exception;
import io.cucumber.messages.types.Group;
import io.cucumber.messages.types.StepDefinition;
import io.cucumber.messages.types.*;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import io.cucumber.plugin.event.*;
import org.testcharm.message.MessageConverterRegistry;
import org.testcharm.util.MapView;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.testcharm.cucumber.swarm.ExceptionSerializer.toThrowable;
import static org.testcharm.util.MapView.*;

@SuppressWarnings("unchecked")
public class EventDeserializer {
    public final MasterDataMapper dataMapper;

    public EventDeserializer(MasterDataMapper dataMapper) {
        this.dataMapper = dataMapper;
    }

    public Object deserialize(String json) {
        Map<String, Object> message = (Map<String, Object>) MessageConverterRegistry.jsonConverter().deserialize(json);
        MapView mapView = new MapView(message);

        return mapView.get("data", map().andThen(data -> {
            switch (mapView.get("type", string())) {
                case "io.cucumber.plugin.event.TestCaseStarted":
                    return new TestCaseStarted(data.get(timeInstant()), data.get(eventTestCase()));
                case "io.cucumber.plugin.event.TestCaseFinished":
                    return new TestCaseFinished(data.get(timeInstant()), data.get(eventTestCase()),
                            data.get(eventResult()));
                case "io.cucumber.plugin.event.TestStepStarted":
                    return new TestStepStarted(data.get(timeInstant()), data.get(eventTestCase()),
                            data.get(eventTestStep()));
                case "io.cucumber.plugin.event.TestStepFinished":
                    return new TestStepFinished(data.get(timeInstant()), data.get(eventTestCase()),
                            data.get(eventTestStep()), data.get(eventResult()));
                case "io.cucumber.messages.types.TestCase": {
                    TestCase testCase = data.get(eventTestCase());
                    List<io.cucumber.messages.types.TestStep> testSteps = data.get("testSteps", indexedList(index -> map().andThen(testStepData -> {
                        TestStep testStep = testCase.getTestSteps().get(index);
                        String hookId = testStepData.get("hookId", string().andThen(dataMapper::hook).andThen(Hook::getId));
                        String pickleStepId = testStep instanceof PickleStepTestStep ? ((Step) ((PickleStepTestStep) testStep).getStep()).getId() : null;
                        List<String> stepDefinitionIds = testStepData.get("stepDefinitionIds",
                                list(string().andThen(dataMapper::stepDefinition).andThen(StepDefinition::getId)));
                        List<StepMatchArgumentsList> stepMatchArgumentsLists = testStepData.get("stepMatchArgumentsLists",
                                list(map().andThen(stepMatchArgumentsListData -> new StepMatchArgumentsList(
                                        stepMatchArgumentsListData.get("stepMatchArguments", list(map().andThen(stepMatchArgumentData ->
                                                new StepMatchArgument(stepMatchArgumentData.get("group", map().andThen(typeGroup())),
                                                        stepMatchArgumentData.get("parameterTypeName", string())))))))));
                        return new io.cucumber.messages.types.TestStep(hookId, testStep.getId().toString(),
                                pickleStepId, stepDefinitionIds, stepMatchArgumentsLists);
                    })));
                    return Envelope.of(new io.cucumber.messages.types.TestCase(
                            testCase.getId().toString(),
                            data.get("testCase", string().andThen(dataMapper::pickle)).getId(),
                            testSteps, data.get("testRunStartedId")));
                }
                case "io.cucumber.messages.types.TestCaseStarted":
                    return Envelope.of(new io.cucumber.messages.types.TestCaseStarted(
                            data.get("attempt", toLong()), data.get("id"),
                            data.get("testCaseId", string().andThen(dataMapper::testCase)).getId().toString(),
                            data.get("workerId"), data.get(timestamp())
                    ));
                case "io.cucumber.messages.types.TestStepStarted": {
                    return Envelope.of(new io.cucumber.messages.types.TestStepStarted(
                            data.get("testCaseStartedId"),
                            data.get(typeTestStepId()),
                            data.get(timestamp())));
                }
                case "io.cucumber.messages.types.TestStepFinished":
                    return Envelope.of(new io.cucumber.messages.types.TestStepFinished(data.get("testCaseStartedId"),
                            data.get(typeTestStepId()),
                            data.get("result", map().andThen(resultData -> new TestStepResult(
                                    resultData.get("duration", map().andThen(durationData -> new io.cucumber.messages.types.Duration(
                                            durationData.get(seconds()), durationData.get(nanos())))),
                                    resultData.get("message"),
                                    resultData.get("status", enumOf(TestStepResultStatus.class)),
                                    resultData.get("exception", map().andThen(exceptionData -> new Exception(exceptionData.get("type"),
                                            exceptionData.get("message"), exceptionData.get("stackTrace"))))))),
                            data.get(timestamp())));
                case "io.cucumber.messages.types.TestCaseFinished":
                    return Envelope.of(new io.cucumber.messages.types.TestCaseFinished(
                            data.get("testCaseStartedId"), data.get(timestamp()), data.get("willBeRetried")));
                default:
                    throw new IllegalArgumentException("Unsupported event type: " + message.get("type"));
            }
        }));
    }

    public Function<MapView, Instant> timeInstant() {
        return map -> map.get("timeInstant", toLong().andThen(Instant::ofEpochMilli));
    }

    public Function<MapView, TestCase> eventTestCase() {
        return map -> dataMapper.testCase(map.get("testCase"));
    }

    public Function<MapView, TestStep> eventTestStep() {
        return composite(eventTestCase(), map -> map.get("testStep", toInt()),
                (testCase, testStepIndex) -> testCase.getTestSteps().get(testStepIndex));
    }

    public Function<MapView, Result> eventResult() {
        return map -> map.get("result", map().andThen(parser -> new Result(
                parser.get("status", enumOf(Status.class)),
                parser.get("duration", toLong().andThen(Duration::ofMillis)),
                parser.get("error", toThrowable()))));
    }

    public Function<MapView, Group> typeGroup() {
        return map -> new Group(map.get("children", list(map().andThen(typeGroup()))),
                map.get("start", toLong()), map.get("value"));
    }

    public Function<MapView, Long> seconds() {
        return map -> map.get("seconds", toLong());
    }

    public Function<MapView, Long> nanos() {
        return map -> map.get("nanos", toLong());
    }

    public Function<MapView, Timestamp> timestamp() {
        return map -> map.get("timestamp", map().andThen(timestampData ->
                new Timestamp(timestampData.get(seconds()), timestampData.get(nanos()))));
    }

    public Function<MapView, String> typeTestStepId() {
        return composite(eventTestCase(), mapView -> mapView.get("testStepId", toInt()),
                (testCase, testStepIndex) -> testCase.getTestSteps().get(testStepIndex).getId().toString());
    }
}
