package org.testcharm.cucumber.swarm.master;

import com.sun.net.httpserver.HttpServer;
import org.testcharm.cucumber.swarm.EntityMapper;
import org.testcharm.cucumber.swarm.repo.WorkerRepository;
import org.testcharm.util.Sneaky;

import java.net.InetSocketAddress;
import java.util.Optional;

public class Server {
    private final Scheduler scheduler;
    private final WorkerRepository workerRepository;
    private final HttpServer httpServer;


    public Server(Scheduler scheduler, WorkerRepository workerRepository, int port) {
        this.scheduler = scheduler;
        this.workerRepository = workerRepository;
        httpServer = Sneaky.get(() -> HttpServer.create(new InetSocketAddress(port), 0));
        httpServer.start();
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

    public void exit() {
        httpServer.stop(0);
    }
}
