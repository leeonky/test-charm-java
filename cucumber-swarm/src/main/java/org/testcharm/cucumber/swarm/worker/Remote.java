package org.testcharm.cucumber.swarm.worker;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.testcharm.cucumber.swarm.EntityMapper;

import java.util.Iterator;
import java.util.List;

public class Remote {
    private final Logger log = LoggerFactory.getLogger(Remote.class);

    public static Remote REMOTE;

    public static void setupRemote(Client client, int workerId, EntityMapper entityMapper) {
        REMOTE = new Remote(client, workerId, entityMapper);
    }

    private final Client client;
    private final EntityMapper entityMapper;
    private final int workerId;

    public Remote(Client client, int workerId, EntityMapper entityMapper) {
        this.client = client;
        this.workerId = workerId;
        this.entityMapper = entityMapper;
    }

    public void setupMapping(List<Feature> features) {
        entityMapper.mapGherkinFeatures(features);
    }

    public Iterable<Pickle> pickles() {
        return () -> new Iterator<Pickle>() {
            private String pickleKey;

            @Override
            public boolean hasNext() {
                log.info(() -> "Requesting pickle...");
                try {
                    pickleKey = client.httpGet(workerId, "/pickle");
                    log.info(() -> String.format("Received pickle<%s>", pickleKey));
                    return true;
                } catch (HttpException ig) {
                    log.info(() -> "No pickle received");
                    return false;
                }
            }

            @Override
            public Pickle next() {
                return entityMapper.pickle(pickleKey);
            }
        };
    }

//    public Pickle requestPickle() {
//        return entityMapper.pickle(client.requestPickle(workerId));
//    }
//
//    public void setupMapping(List<Feature> features) {
//        entityMapper.mapGherkinFeatures(features);
//    }
//
//    public boolean register() {
//        workerId = client.register().orElse(NOT_EXIST);
//        return workerId != NOT_EXIST;
//    }

    //TODO test: should not forward worker testRunFinished and testRunStarted
//    public void sendEvent(Object event) {
//        client.sendEvent(workerId, event);
//    }
}
