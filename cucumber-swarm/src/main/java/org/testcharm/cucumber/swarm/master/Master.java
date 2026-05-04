package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.testcharm.cucumber.swarm.EntityMapper;
import org.testcharm.cucumber.swarm.SwarmArgs;
import org.testcharm.cucumber.swarm.repo.Repository;
import org.testcharm.util.Sneaky;

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

    public Master(SwarmArgs swarmArgs, List<Pickle> pickles, EntityMapper entityMapper) {
        this.swarmArgs = swarmArgs;
        pickleQueue = new ConcurrentLinkedQueue<>(pickles);
        controller = new Controller(this, workers, entityMapper, new RestfulServer(swarmArgs.getSwarmHost().getPort()));
        log.info(() -> String.format("Master created with %d scenarios", pickleQueue.size()));
    }

    public void start() {
        controller.start();
        workers.save(new LocalWorker(swarmArgs));
        while (!pickleQueue.isEmpty())
            Sneaky.run(() -> Thread.sleep(50));
        log.info(() -> "Pickle queue EMPTY");
    }

    public void shutdown() {
        log.info(() -> "Shutting down master...");
        for (Worker worker : workers.findAll()) {
            log.info(() -> String.format("Waiting and collecting worker<%d> exit status", worker.id()));
            worker.shutdown();
        }
        controller.shutdown();
        log.info(() -> "Master shut down");
    }

    public Pickle requestPickle(Worker worker) {
        Pickle poll = pickleQueue.poll();
        if (poll != null)
            return poll;
        log.info(() -> "No more pickles");
        throw new NoSuchElementException();
    }
}
