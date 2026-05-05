package org.testcharm.cucumber.swarm.worker;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseStarted;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.cucumber.swarm.DataMapper;
import org.testcharm.cucumber.swarm.master.EventDeserializer;
import org.testcharm.cucumber.swarm.master.MasterDataMapper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testcharm.dal.Assertions.expect;

class EventSerializerTest {
    private final Path executorRoot = Paths.get("/executor/");
    private final EventSerializer eventSerializer = new EventSerializer(new DataMapper(singletonList(executorRoot.toUri())));

    private final Path masterRoot = Paths.get("/master/");
    private final MasterDataMapper masterDataMapper = new MasterDataMapper(singletonList(masterRoot.toUri()), null);
    private final EventDeserializer eventDeserializer = new EventDeserializer(masterDataMapper);

    @Nested
    class SerializeTestCaseStarted {

        @Test
        void serializes_test_case_started_event() {
            TestCase executorTestCase = mock(TestCase.class);
            when(executorTestCase.getLocation()).thenReturn(new Location(100, 0));
            when(executorTestCase.getUri()).thenReturn(executorRoot.resolve("features/test.feature").toUri());

            TestCase masterTestCase = mock(TestCase.class);
            when(masterTestCase.getLocation()).thenReturn(new Location(100, 0));
            when(masterTestCase.getUri()).thenReturn(masterRoot.resolve("features/test.feature").toUri());

            masterDataMapper.mapTestCase(masterTestCase);

            TestCaseStarted event = new TestCaseStarted(Instant.ofEpochMilli(1000), executorTestCase);

            expect(eventDeserializer.deserialize(eventSerializer.serialize(event)))
                    .should(": { class.simpleName: TestCaseStarted, instant: '1970-01-01T00:00:01Z', testCase: { location: { line: 100, column: 0 }, uri: 'file:///master/features/test.feature' } }");
        }
    }
}