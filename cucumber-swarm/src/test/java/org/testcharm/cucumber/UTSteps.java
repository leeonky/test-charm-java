package org.testcharm.cucumber;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.plugin.event.TestCase;
import org.testcharm.cucumber.builder.Builder;
import org.testcharm.cucumber.swarm.master.EventDeserializer;
import org.testcharm.cucumber.swarm.master.MasterDataMapper;
import org.testcharm.cucumber.swarm.worker.EventSerializer;
import org.testcharm.cucumber.swarm.worker.WorkerDataMapper;
import org.testcharm.jfactory.JFactory;
import org.testcharm.jfactory.Spec;
import org.testcharm.jfactory.cucumber.JData;
import org.testcharm.util.Classes;
import org.testcharm.util.Sneaky;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testcharm.dal.Assertions.expect;

public class UTSteps {
    UTTestContext utTestContext;

    @Before
    public void beforeEach() {
        utTestContext = new UTTestContext();
    }

    @Given("master side {string}:")
    public void master_side(String spec, JData.DocData docData) {
        utTestContext.mapMaster(spec, docData);
    }

    @When("serialize the following {string} in executor side and deserialize in master side:")
    public void serialize_the_following_in_executor_side_and_deserialize_in_master_side(String string, JData.DocData docData) {
        utTestContext.serializeAndDeserializeSingle(string, docData);
    }

    @Then("the deserialized object should be:")
    public void the_deserialized_object_should_be(String docString) {
        utTestContext.verify(docString);
    }

    public static class UTTestContext {
        private final Path executorRoot = Paths.get("/executor/");
        private final EventSerializer eventSerializer = new EventSerializer(new WorkerDataMapper(executorRoot.toString()));

        private final Path masterRoot = Paths.get("/master/");
        private final MasterDataMapper masterDataMapper = new MasterDataMapper(null, masterRoot.toString());
        private final EventDeserializer eventDeserializer = new EventDeserializer(masterDataMapper);
        private final JFactory jFactory = new JFactory();
        private final JData jData = new JData(jFactory);

        private Object object;

        public UTTestContext() {
            Classes.subTypesOf(Spec.class, "org.testcharm.cucumber").forEach(c -> jFactory.register(Sneaky.cast(c)));
        }

        private void mapMaster(Object object) {
            if (object instanceof TestCase)
                masterDataMapper.mapTestCase((TestCase) object);
        }

        public void mapMaster(String spec, JData.DocData docData) {
            for (Object object : jData.prepare(spec, docData)) {
                mapMaster(object);
            }
        }

        public void serializeAndDeserializeSingle(String spec, JData.DocData docData) {
            Object object1 = jData.prepare(spec, docData).get(0);
            if (object1 instanceof Builder)
                object = eventDeserializer.deserialize(eventSerializer.serialize(((Builder) object1).build()));
            else
                object = eventDeserializer.deserialize(eventSerializer.serialize(object1));
        }

        public void verify(String docString) {
            expect(object).should(docString);
        }
    }
}
