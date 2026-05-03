package org.testcharm.cucumber.swarm.master;

import org.testcharm.cucumber.swarm.repo.WorkerDeprecatedRepository;

public class Server {
//    private final Master master;
//    private final WorkerDeprecatedRepository workerDeprecatedRepository;
//    private final RestfulSever restfulSever;


    public Server(Master master, WorkerDeprecatedRepository workerDeprecatedRepository, int port) {
//        this.master = master;
//        this.workerDeprecatedRepository = workerDeprecatedRepository;
//        restfulSever = new RestfulSever(port);
//        setupRoute();
//        restfulSever.start();
    }

    private void setupRoute() {
//        restfulSever.requestHandler("POST", "/register", context ->
//                context.responseOk(String.valueOf(master.register().id())));
//
//        restfulSever.requestHandler("GET", "/pickle", context -> {
//            WorkerDeprecated workerDeprecated = workerDeprecatedRepository.findById(Integer.parseInt(context.header("X-Worker-Id")));
//            context.responseOk(pickleKey(master.requestPickle(workerDeprecated)));
//        });
    }
}
