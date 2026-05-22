package org.testcharm.cucumber.swarm.worker;

import io.cucumber.messages.types.TestStepResult;
import io.cucumber.messages.types.Timestamp;
import io.cucumber.plugin.event.*;
import org.testcharm.message.MessageConverterRegistry;
import org.testcharm.util.MapView;

import java.util.Map;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static org.testcharm.cucumber.swarm.ExceptionSerializer.serializeError;
import static org.testcharm.util.MapView.mapView;

public class EventSerializer {
    public final WorkerDataMapper dataMapper;

    public EventSerializer(WorkerDataMapper dataMapper) {
        this.dataMapper = dataMapper;
    }

    private Map<String, Object> serializeEvent(Object event) {
        MapView data = mapView();
        switch (event.getClass().getName()) {
            case "io.cucumber.plugin.event.TestStepFinished":
                return data.set(eventTestCaseEvent((TestCaseEvent) event))
                        .set(eventTestStep(((TestStepFinished) event).getTestCase(), ((TestStepFinished) event).getTestStep()))
                        .set(eventResult(((TestStepFinished) event).getResult()));
            case "io.cucumber.plugin.event.TestStepStarted":
                return data.set(eventTestCaseEvent((TestCaseEvent) event))
                        .set(eventTestStep(((TestStepStarted) event).getTestCase(), ((TestStepStarted) event).getTestStep()));
            case "io.cucumber.plugin.event.TestCaseFinished":
                return data.set(eventTestCaseEvent((TestCaseEvent) event))
                        .set(eventResult(((TestCaseFinished) event).getResult()));
            case "io.cucumber.plugin.event.TestCaseStarted":
                return data.set(eventTestCaseEvent((TestCaseEvent) event));
            case "io.cucumber.messages.types.TestCase":
                return data.set(typeTestCase((io.cucumber.messages.types.TestCase) event));
            case "io.cucumber.messages.types.TestCaseStarted":
                io.cucumber.messages.types.TestCaseStarted testCaseStarted = (io.cucumber.messages.types.TestCaseStarted) event;
                return data.set("attempt", testCaseStarted.getAttempt())
                        .set("id", testCaseStarted.getId())
                        .set("testCaseId", dataMapper.transformTestCaseIdToKey(testCaseStarted.getTestCaseId()))
                        .set("workerId", testCaseStarted.getWorkerId())
                        .set(timestamp(testCaseStarted.getTimestamp()));
            case "io.cucumber.messages.types.TestStepStarted":
                io.cucumber.messages.types.TestStepStarted testStepStartedMessage = (io.cucumber.messages.types.TestStepStarted) event;
                return data
                        .set("testCaseStartedId", testStepStartedMessage.getTestCaseStartedId())
                        .set(eventTestCaseForTypeTestStepId(testStepStartedMessage.getTestStepId()))
                        .set(typeTestStepId(testStepStartedMessage.getTestStepId()))
                        .set(timestamp(testStepStartedMessage.getTimestamp()));
            case "io.cucumber.messages.types.TestStepFinished":
                io.cucumber.messages.types.TestStepFinished testStepFinishedMessage = (io.cucumber.messages.types.TestStepFinished) event;
                TestStepResult testStepResult = testStepFinishedMessage.getTestStepResult();
                return data.set("testCaseStartedId", testStepFinishedMessage.getTestCaseStartedId())
                        .set(eventTestCaseForTypeTestStepId(testStepFinishedMessage.getTestStepId()))
                        .set(typeTestStepId(testStepFinishedMessage.getTestStepId()))
                        .set("result", mapView()
                                .set("duration", mapView()
                                        .set("seconds", testStepResult.getDuration().getSeconds())
                                        .set("nanos", testStepResult.getDuration().getNanos()))
                                .set("message", testStepResult.getMessage())
                                .set("status", testStepResult.getStatus().name())
                                .set("exception", testStepResult.getException().map(e -> mapView()
                                        .set("type", e.getType())
                                        .set("message", e.getMessage())
                                        .set("stackTrace", e.getStackTrace()))))
                        .set(timestamp(testStepFinishedMessage.getTimestamp()));
            case "io.cucumber.messages.types.TestCaseFinished": {
                io.cucumber.messages.types.TestCaseFinished testCaseFinishedMessage = (io.cucumber.messages.types.TestCaseFinished) event;
                return data
                        .set("testCaseStartedId", testCaseFinishedMessage.getTestCaseStartedId())
                        .set(timestamp(testCaseFinishedMessage.getTimestamp()))
                        .set("willBeRetried", testCaseFinishedMessage.getWillBeRetried());
            }
            default:
                throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
        }
    }

    private Consumer<MapView> eventTestCaseEvent(TestCaseEvent event) {
        return map -> map.set("testCase", dataMapper.testCaseKey(event.getTestCase()))
                .set("timeInstant", event.getInstant().toEpochMilli());
    }

    private Consumer<MapView> eventResult(Result result) {
        return map -> map.set("result", mapView()
                .set("status", result.getStatus().name())
                .set("duration", result.getDuration().toMillis())
                .set("error", serializeError(result.getError())));
    }

    private Consumer<MapView> eventTestStep(TestCase testCase, TestStep testStep) {
        return map -> map.set("testStep", testCase.getTestSteps().indexOf(testStep));
    }

    private Consumer<MapView> typeTestCase(io.cucumber.messages.types.TestCase testCase) {
        return map -> map.set("testCase", dataMapper.pickleKey(dataMapper.pickleById(testCase.getPickleId())))
                .set("testSteps", testCase.getTestSteps().stream().map(this::serializeTestStep));
    }

    private MapView serializeTestStep(io.cucumber.messages.types.TestStep testStep) {
        return mapView()
                .set("hookId", testStep.getHookId().map(dataMapper::transformHookIdToKey))
                .set("stepDefinitionIds", testStep.getStepDefinitionIds().map(ids ->
                        ids.stream().map(dataMapper::transformStepDefinitionIdToKey)))
                .set("stepMatchArgumentsLists", testStep.getStepMatchArgumentsLists().map(stepMatchArgumentsLists ->
                        stepMatchArgumentsLists.stream().map(argList -> mapView()
                                .set("stepMatchArguments", argList.getStepMatchArguments().stream().map(arg -> mapView()
                                        .set("group", serializeGroup(arg.getGroup()))
                                        .set("parameterTypeName", arg.getParameterTypeName()))))));
    }

    private MapView serializeGroup(io.cucumber.messages.types.Group group) {
        return mapView().set("children", group.getChildren().stream().map(this::serializeGroup))
                .set("start", group.getStart())
                .set("value", group.getValue());
    }

    private Consumer<MapView> timestamp(Timestamp timestamp) {
        return map -> map.set("timestamp", mapView()
                .set("seconds", timestamp.getSeconds())
                .set("nanos", timestamp.getNanos()));
    }

    private Consumer<MapView> eventTestCaseForTypeTestStepId(String typeTestStepId) {
        return map -> map.set("testCase", dataMapper.pickleKey(dataMapper.pickleById(
                dataMapper.testCaseByStepId(typeTestStepId).getPickleId())));

    }

    private Consumer<MapView> typeTestStepId(String typeTestStepId) {
        int testStepIdIndex = dataMapper.testCaseByStepId(typeTestStepId).getTestSteps().stream()
                .map(io.cucumber.messages.types.TestStep::getId)
                .collect(toList()).indexOf(typeTestStepId);
        return map -> map.set("testStepId", testStepIdIndex);
    }

    public String serialize(Object event) {
        return MessageConverterRegistry.jsonConverter().serialize(mapView()
                .set("type", event.getClass().getName())
                .set("data", serializeEvent(event)));
    }
}
