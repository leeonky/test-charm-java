package org.testcharm.cucumber.swarm.worker;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.testcharm.util.IterableBlockingQueue;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class Remote {
    private final Logger log = LoggerFactory.getLogger(Remote.class);

    public static Remote REMOTE;

    public static void setupRemote(RestfulClient restfulClient, int workerId, WorkerDataMapper dataMapper) {
        REMOTE = new Remote(restfulClient, workerId, dataMapper);
    }

    private final RestfulClient restfulClient;
    private final WorkerDataMapper dataMapper;
    private final int workerId;
    private final EventSerializer eventSerializer;

    public Remote(RestfulClient restfulClient, int workerId, WorkerDataMapper dataMapper) {
        this.restfulClient = restfulClient;
        this.workerId = workerId;
        this.dataMapper = dataMapper;
        eventSerializer = new EventSerializer(dataMapper);
    }

    public void setupMapping(List<Feature> features) {
        features.forEach(dataMapper::mapGherkinFeature);
    }

    public Iterable<Pickle> pickles() {
        IterableBlockingQueue<Pickle> pickles = new IterableBlockingQueue<>(new ArrayBlockingQueue<>(1));
        new Thread(() -> {
            for (; ; ) {
                try {
                    log.info(() -> "Requesting pickle...");
                    String pickleKey = restfulClient.httpGet(workerId, "/pickle");
                    pickles.put(dataMapper.pickle(pickleKey));
                    log.info(() -> String.format("Received pickle<%s>", pickleKey));
                } catch (HttpException ig) {
                    log.info(() -> "No pickle received");
                    pickles.close();
                }
            }
        }).start();
        return pickles;
    }

    public void sendEvent(Object event) {
        String serialize = eventSerializer.serialize(event);
        log.info(() -> String.format("Forwarding event: %s", serialize));
        restfulClient.httpPost(workerId, "/events", serialize);
    }

    public void sayReady() {
        restfulClient.httpPost(workerId, "/ready", "");
    }
}
