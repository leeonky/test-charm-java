package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.testcharm.cucumber.swarm.SwarmArgs;
import org.testcharm.cucumber.swarm.repo.Repository;
import org.testcharm.util.Sneaky;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Master {
    private final Repository<Integer, Worker> workers = new Repository<>(Worker::id);
    private final SwarmArgs swarmArgs;
    private final Logger log = LoggerFactory.getLogger(Master.class);
    private final Controller controller;
    private final Queue<Pickle> pickleQueue;
    private final MasterDataMapper dataMapper;
    private final EventDeserializer eventDeserializer;
    private final EventBus eventBus;

    public Master(SwarmArgs swarmArgs, List<Pickle> pickles, MasterDataMapper dataMapper, EventBus eventBus) {
        this.swarmArgs = swarmArgs;
        pickleQueue = new ConcurrentLinkedQueue<>(pickles);
        this.dataMapper = dataMapper;
        this.eventBus = eventBus;
        controller = new Controller(this, workers, dataMapper, new RestfulServer(swarmArgs.getSwarmHost().getPort()));
        eventDeserializer = new EventDeserializer(dataMapper);
        log.info(() -> String.format("Master created with %d scenarios", pickleQueue.size()));
    }

    public Master setupMapping(List<Feature> features) {
        features.forEach(dataMapper::mapGherkinFeature);
        return this;
    }

    public Master start() {
        int pickleCount = pickleQueue.size();
        Instant now = Instant.now();
        controller.start();
        if (swarmArgs.isLocalWorker())
            workers.save(new LocalWorker(swarmArgs));
        while (!pickleQueue.isEmpty()) {
            if (Instant.now().isAfter(now.plusSeconds(swarmArgs.getWorkerTimeout())) && pickleQueue.size() == pickleCount) {
                String message = String.format("No worker available after waiting for %d seconds", swarmArgs.getWorkerTimeout());
                log.info(() -> message);
                throw new IllegalStateException(message);
            }
            Sneaky.run(() -> Thread.sleep(50));
        }
        log.info(() -> "Pickle queue EMPTY");
        return this;
    }

    public Master shutdown() {
        log.info(() -> "Shutting down master...");
        for (Worker worker : workers.findAll()) {
            log.info(() -> String.format("Waiting and collecting worker<%d> exit status", worker.id()));
            worker.shutdown();
        }
        controller.shutdown();
        log.info(() -> "Master shut down");
        return this;
    }

    public Pickle requestPickle(Worker worker) {
        Pickle poll = pickleQueue.poll();
        if (poll != null)
            return poll;
        log.info(() -> "No more pickles");
        throw new NoSuchElementException();
    }

    public void forwardEvent(String eventRecord) {
        Object deserialize = eventDeserializer.deserialize(eventRecord);
        log.info(() -> "Forwarding event: " + deserialize.getClass().getName());
        eventBus.send(deserialize);
    }
}
