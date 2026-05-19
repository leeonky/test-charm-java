package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.testcharm.cucumber.swarm.DataMapper;
import org.testcharm.cucumber.swarm.repo.Repository;

public class Controller {
    private final Master master;
    private final RestfulServer restfulServer;
    private final Repository<Integer, Worker> workerRepository;
    private final Logger log = LoggerFactory.getLogger(Controller.class);
    private final DataMapper dataMapper;

    public Controller(Master master, Repository<Integer, Worker> workerRepository, DataMapper dataMapper, RestfulServer restfulServer) {
        this.master = master;
        this.restfulServer = restfulServer;
        this.workerRepository = workerRepository;
        this.dataMapper = dataMapper;
        setupRoute();
    }

    private void setupRoute() {
        restfulServer.requestHandler("GET", "/pickle", context -> {
            int workerId = Integer.parseInt(context.header("X-Worker-Id"));
            log.info(() -> String.format("Received worker<%d> pickle request", workerId));
            String pickleKey = dataMapper.pickleKey(master.requestPickle(workerRepository.findByKey(workerId)));
            log.info(() -> String.format("Send pickle<%s> to worker<%d>", pickleKey, workerId));
            context.responseOk(pickleKey);
        });

        restfulServer.requestHandler("POST", "/events", context -> {
            int workerId = Integer.parseInt(context.header("X-Worker-Id"));
            String body = context.body();
            log.info(() -> String.format("Received worker<%d> event: %s", workerId, body));
            master.forwardEvent(body);
            context.responseOk();
        });
    }

    public void start() {
        restfulServer.start();
    }

    public void shutdown() {
        restfulServer.shutdown();
    }
}
