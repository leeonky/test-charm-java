package org.testcharm.cucumber.swarm.worker;

import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStepStarted;
import org.testcharm.cucumber.swarm.DataMapper;
import org.testcharm.cucumber.swarm.ExceptionSerializer;
import org.testcharm.message.MessageConverterRegistry;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
                return content(type, new LinkedHashMap<String, Object>() {{
                    put("testCase", dataMapper.testCaseKey(((TestCaseStarted) event).getTestCase()));
                    put("timeInstant", ((TestCaseStarted) event).getInstant().toEpochMilli());
                }});
            case "io.cucumber.plugin.event.TestCaseFinished":
                return content(type, new LinkedHashMap<String, Object>() {{
                    put("testCase", dataMapper.testCaseKey(((TestCaseFinished) event).getTestCase()));
                    put("timeInstant", ((TestCaseFinished) event).getInstant().toEpochMilli());
                    Result result = ((TestCaseFinished) event).getResult();
                    LinkedHashMap<String, Object> resultData = new LinkedHashMap<String, Object>() {{
                        put("status", result.getStatus().name());
                        put("duration", result.getDuration().toMillis());
                    }};
                    ExceptionSerializer.serialize(result.getError(), resultData, "error");
                    put("result", resultData);
                }});
            case "io.cucumber.plugin.event.TestStepStarted":
                TestStepStarted testStepStarted = (TestStepStarted) event;
                return content(type, new LinkedHashMap<String, Object>() {{
                    put("testCase", dataMapper.testCaseKey(testStepStarted.getTestCase()));
                    put("timeInstant", testStepStarted.getInstant().toEpochMilli());
                    put("testStep", testStepStarted.getTestCase().getTestSteps().indexOf(testStepStarted.getTestStep()));
                }});
            default:
                throw new IllegalArgumentException("Unsupported event type: " + type);
        }
    }

    private String content(String type, final Map<String, Object> data) {
        return MessageConverterRegistry.jsonConverter().serialize(new LinkedHashMap<String, Object>() {{
            put("type", type);
            put("data", data);
        }});
    }
}
