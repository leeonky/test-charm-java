package org.testcharm.cucumber.swarm.repo;

import org.testcharm.cucumber.swarm.master.Worker;

public class WorkerRepository extends Repository<Integer, Worker> {
    public WorkerRepository() {
        super(Worker::id);
    }
}
