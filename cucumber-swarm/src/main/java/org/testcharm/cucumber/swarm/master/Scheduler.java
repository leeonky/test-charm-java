package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Pickle;
import org.testcharm.cucumber.swarm.EntityMapper;
import org.testcharm.cucumber.swarm.repo.WorkerRepository;
import org.testcharm.util.Sneaky;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Scheduler {
    private final EventBus eventBus;
    private final BlockingQueue<Worker> idleWorkers = new LinkedBlockingQueue<>();
    private final WorkerRepository workerRepository = new WorkerRepository();
    private final Server server;
    private final EntityMapper entityMapper = new EntityMapper();

    private boolean running = true;

    public Scheduler(EventBus eventBus, int port) {
        this.eventBus = eventBus;
        server = new Server(this, workerRepository, port);
    }

    public synchronized Worker register() {
        if (running)
            return workerRepository.save(new Worker());
        throw new ServerCloseException();
    }

    public void responsePickle(Pickle pickle) {
        Sneaky.get(idleWorkers::take).responsePickle(pickle);
    }

    public Pickle requestPickle(Worker worker) {
        Sneaky.run(() -> idleWorkers.put(worker));
        return worker.requestPickle();
    }

    public synchronized void exitAll() {
        running = false;
        workerRepository.findAll().forEach(ig -> Sneaky.get(idleWorkers::take).exit());
        server.exit();
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
