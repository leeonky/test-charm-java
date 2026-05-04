package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.testcharm.cucumber.swarm.EntityMapper;
import org.testcharm.cucumber.swarm.repo.Repository;

public class Controller {
    private final Master master;
    private final RestfulServer restfulServer;
    private final Repository<Integer, Worker> workerRepository;
    private final Logger log = LoggerFactory.getLogger(Controller.class);
    private final EntityMapper entityMapper;

    public Controller(Master master, Repository<Integer, Worker> workerRepository, EntityMapper entityMapper, RestfulServer restfulServer) {
        this.master = master;
        this.restfulServer = restfulServer;
        this.workerRepository = workerRepository;
        this.entityMapper = entityMapper;
        setupRoute();
    }

    private void setupRoute() {
//        restfulServer.requestHandler("POST", "/register", context ->
//                context.responseOk(String.valueOf(master.register().id())));
//
        restfulServer.requestHandler("GET", "/pickle", context -> {
            int workerId = Integer.parseInt(context.header("X-Worker-Id"));
            log.info(() -> String.format("Received worker<%d> pickle request", workerId));
            String pickleKey = entityMapper.pickleKey(master.requestPickle(workerRepository.findById(workerId)));
            log.info(() -> String.format("Send pickle<%s> to worker<%d>", pickleKey, workerId));
            context.responseOk(pickleKey);
        });
    }

    public void start() {
        restfulServer.start();
    }

    public void shutdown() {
        restfulServer.shutdown();
    }
}
