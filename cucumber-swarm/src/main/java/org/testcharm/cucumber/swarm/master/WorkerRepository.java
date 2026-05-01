package org.testcharm.cucumber.swarm.master;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerRepository {
    private final Map<Integer, Worker> workerRepo = new ConcurrentHashMap<>();

    public Worker save(Worker worker) {
        workerRepo.put(worker.id(), worker);
        return worker;
    }

    public Collection<Worker> findAll() {
        return workerRepo.values();
    }

    public boolean isEmpty() {
        return workerRepo.isEmpty();
    }

    public Worker findById(int id) {
        return workerRepo.get(id);
    }
}