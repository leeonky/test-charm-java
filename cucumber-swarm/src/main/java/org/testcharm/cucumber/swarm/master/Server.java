package org.testcharm.cucumber.swarm.master;

import org.testcharm.cucumber.swarm.EntityMapper;
import org.testcharm.cucumber.swarm.repo.WorkerRepository;

public class Server {
    private final Scheduler scheduler;
    private final WorkerRepository workerRepository;
    private final RestfulSever restfulSever;


    public Server(Scheduler scheduler, WorkerRepository workerRepository, int port) {
        this.scheduler = scheduler;
        this.workerRepository = workerRepository;
        restfulSever = new RestfulSever(port);
        setupRoute();
        restfulSever.start();
    }

    private void setupRoute() {
        restfulSever.requestHandler("POST", "/register", context ->
                context.responseOk(String.valueOf(scheduler.register().get().id())));
    }

    public String requestPickle(int workerId) {
        return EntityMapper.pickleKey(scheduler.requestPickle(workerRepository.findById(workerId)));
    }

    public void receiveEvent(int workerId, Object event) {
        scheduler.receiveEvent(workerRepository.findById(workerId), event);
    }

    public void exit() {
        restfulSever.exit();
    }
}
