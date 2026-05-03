package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.testcharm.cucumber.swarm.EntityMapper;
import org.testcharm.cucumber.swarm.repo.Repository;
import org.testcharm.cucumber.swarm.worker.LocalWorker;
import org.testcharm.cucumber.swarm.worker.Worker;

public class Master {
    private final EntityMapper entityMapper = new EntityMapper();
    private final Repository<Integer, Worker> workers = new Repository<>(Worker::id);
    private Logger log = LoggerFactory.getLogger(Master.class);

    public Master() {
        log.info(() -> "Master created");
        Worker worker = workers.save(new LocalWorker());
        worker.start();
    }

    public void waitJobsDone() {
        for (Worker worker : workers.findAll()) {
            worker.stop();
        }
    }
}
