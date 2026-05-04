package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.testcharm.cucumber.swarm.SwarmArgs;
import org.testcharm.cucumber.swarm.repo.Repository;

import java.util.List;

public class Master {
    //    private final EntityMapper entityMapper = new EntityMapper();
    private final Repository<Integer, Worker> workers = new Repository<>(Worker::id);
    private final SwarmArgs swarmArgs;
    private final Logger log = LoggerFactory.getLogger(Master.class);
    private final Controller controller;

    public Master(SwarmArgs swarmArgs, List<Pickle> pickles) {
        this.swarmArgs = swarmArgs;
        log.info(() -> String.format("Master created with %d scenarios", pickles.size()));
        controller = new Controller(this, swarmArgs.getSwarmHost().getPort());
    }

    public void start() {
        controller.start();
        workers.save(new LocalWorker(swarmArgs));
    }

    public void shutdown() {
        log.info(() -> "Shutting down master...");
        for (Worker worker : workers.findAll()) {
            log.info(() -> String.format("Collecting worker<%d> exit status", worker.id()));
            worker.shutdown();
        }
        controller.shutdown();
        log.info(() -> "Master shut down");
    }
}
