package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Pickle;
import org.testcharm.util.Sneaky;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DistributedPickleScheduler {
    private final EventBus eventBus;
    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<>());
    private final BlockingQueue<Worker> idleWorkers = new LinkedBlockingQueue<>();
    private final Server server = new Server(this);
    private boolean running = true;

    public DistributedPickleScheduler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public synchronized boolean register(Worker worker) {
        if (running) {
            workers.add(worker);
        }
        return running;
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
        server.stop();
        workers.forEach(ig -> Sneaky.get(idleWorkers::take).exit());
    }

    @Deprecated
    public Server server() {
        return server;
    }

    public void receiveEvent(Worker worker, Object event) {
        eventBus.send(event);
    }

    public void forceWaitingWorker() {
        for (int i = 0; i < 100 && workers.isEmpty(); i++)
            Sneaky.run(() -> Thread.sleep(10));
    }
}
