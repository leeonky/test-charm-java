package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Pickle;
import org.testcharm.util.Sneaky;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DistributedPickleScheduler {
    private final EventBus eventBus;
    private final BlockingQueue<Worker> idleWorkers = new LinkedBlockingQueue<>();
    private final WorkerRepository workerRepository = new WorkerRepository();
    private final Server server = new Server(this, workerRepository);
    private boolean running = true;

    public DistributedPickleScheduler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public synchronized Optional<Worker> register() {
        if (running)
            return Optional.of(workerRepository.save(new Worker()));
        return Optional.empty();
    }

    public void execute(Pickle pickle) {
        Sneaky.get(idleWorkers::take).responseNewPickle(pickle);
    }

    public Pickle responsePickle(Worker worker) {
        Sneaky.run(() -> idleWorkers.put(worker));
        return worker.requestPickle();
    }

    public synchronized void exitAll() {
        running = false;
        workerRepository.findAll().forEach(ig -> Sneaky.get(idleWorkers::take).exit());
    }

    @Deprecated
    public Server server() {
        return server;
    }

    public void receiveEvent(Worker worker, Object event) {
        eventBus.send(event);
    }

    public void forceWaitingWorker() {
        for (int i = 0; i < 100 && workerRepository.isEmpty(); i++)
            Sneaky.run(() -> Thread.sleep(10));
    }
}
