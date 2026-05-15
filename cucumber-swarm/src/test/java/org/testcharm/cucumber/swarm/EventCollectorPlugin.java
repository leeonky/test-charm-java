package org.testcharm.cucumber.swarm;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import org.testcharm.io.TempDirectory;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testcharm.dal.Assertions.expect;

public class EventCollectorPlugin implements ConcurrentEventListener {
    Map<String, List<Object>> events = new HashMap<>();
    private String index;

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseStarted.class, event -> {
            String path = event.getTestCase().getUri().toString();
            int start = path.indexOf("src/test/generate/") + "src/test/generate/".length();
            int end = path.indexOf("/cucumber/features");
            index = path.substring(start, end);
        });

        publisher.registerHandlerFor(TestCaseStarted.class, this::saveEvent);
        publisher.registerHandlerFor(TestStepStarted.class, this::saveEvent);
        publisher.registerHandlerFor(TestStepFinished.class, this::saveEvent);
        publisher.registerHandlerFor(TestCaseFinished.class, this::saveEvent);

        publisher.registerHandlerFor(TestRunFinished.class, event -> {
            if (index != null) {
                TempDirectory dir = new TempDirectory(Paths.get("src", "test", "generate", index)).mkdir("dal");
                if (dir.exist("verify.dal")) {
                    try {
                        expect(events).should(dir.readAllText("verify.dal"));
                        dir.write("passed", "");
                    } catch (Throwable e) {
                        dir.write("failed", e.getMessage());
                    }
                }
            }
        });

    }

    private void saveEvent(Object event) {
        events.computeIfAbsent(event.getClass().getName(), k -> new java.util.ArrayList<>()).add(event);
    }
}
