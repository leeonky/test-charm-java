package org.testcharm.cucumber.swarm.worker;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.testcharm.cucumber.swarm.EntityMapper;
import org.testcharm.cucumber.swarm.master.Master;

import java.util.Iterator;
import java.util.List;

public class Remote {
    private final Logger log = LoggerFactory.getLogger(Remote.class);

    public static Remote REMOTE;

    public static void setupRemote(RestfulClient restfulClient, int workerId, EntityMapper entityMapper) {
        REMOTE = new Remote(restfulClient, workerId, entityMapper);
    }

    private final RestfulClient restfulClient;
    private final EntityMapper entityMapper;
    private final int workerId;

    public Remote(RestfulClient restfulClient, int workerId, EntityMapper entityMapper) {
        this.restfulClient = restfulClient;
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
                return entityMapper.pickle(pickleKey);
            }
        };
    }

    //TODO test: should not forward worker testRunFinished and testRunStarted
    public void sendEvent(Object event) {
//        TODO need use restful api
        Master.eventBus.send(event);
    }
}
