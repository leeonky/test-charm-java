package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.gherkin.Pickle;
import org.testcharm.cucumber.swarm.repo.WorkerRepository;

import java.util.Optional;

public class Server {
    private final DistributedPickleScheduler scheduler;
    private final WorkerRepository workerRepository;

    public Server(DistributedPickleScheduler scheduler, WorkerRepository workerRepository) {
        this.scheduler = scheduler;
        this.workerRepository = workerRepository;
    }

    public Pickle responsePickle(int workerId) {
        return scheduler.responsePickle(workerRepository.findById(workerId));
    }

    public Optional<Integer> register() {
        return scheduler.register().map(Worker::id);
    }

    public void receiveEvent(int workerId, Object event) {
        scheduler.receiveEvent(workerRepository.findById(workerId), event);
    }
}
