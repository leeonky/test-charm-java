package org.testcharm.cucumber.swarm.master;

import org.testcharm.cucumber.swarm.EntityMapper;
import org.testcharm.cucumber.swarm.repo.WorkerRepository;

import java.util.Optional;

public class Server {
    private final Scheduler scheduler;
    private final WorkerRepository workerRepository;

    public Server(Scheduler scheduler, WorkerRepository workerRepository) {
        this.scheduler = scheduler;
        this.workerRepository = workerRepository;
    }

    public String requestPickle(int workerId) {
        return EntityMapper.pickleKey(scheduler.requestPickle(workerRepository.findById(workerId)));
    }

    public Optional<Integer> register() {
        return scheduler.register().map(Worker::id);
    }

    public void receiveEvent(int workerId, Object event) {
        scheduler.receiveEvent(workerRepository.findById(workerId), event);
    }
}
