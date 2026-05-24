package org.testcharm.cucumber.swarm.worker;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.util.Iterator;
import java.util.List;

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
        return () -> new Iterator<Pickle>() {
            private String pickleKey;

            @Override
            public boolean hasNext() {
                log.info(() -> "Requesting pickle...");
                try {
                    pickleKey = restfulClient.httpGet(workerId, "/pickle");
                    log.info(() -> String.format("Received pickle<%s>", pickleKey));
                    return true;
                } catch (HttpException ig) {
                    log.info(() -> "No pickle received");
                    return false;
                }
            }

            @Override
            public Pickle next() {
                return dataMapper.pickle(pickleKey);
            }
        };
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
