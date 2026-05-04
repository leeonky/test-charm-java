package org.testcharm.cucumber.swarm.master;

public class Controller {
    private final Master master;
    private final RestfulServer restfulServer;


    public Controller(Master master, int port) {
        this.master = master;
        restfulServer = new RestfulServer(port);
        setupRoute();
    }

    private void setupRoute() {
//        restfulServer.requestHandler("POST", "/register", context ->
//                context.responseOk(String.valueOf(master.register().id())));
//
//        restfulServer.requestHandler("GET", "/pickle", context -> {
//            WorkerDeprecated workerDeprecated = workerDeprecatedRepository.findById(Integer.parseInt(context.header("X-Worker-Id")));
//            context.responseOk(pickleKey(master.requestPickle(workerDeprecated)));
//        });
    }

    public void start() {
        restfulServer.start();
    }

    public void shutdown() {
        restfulServer.shutdown();
    }
}
