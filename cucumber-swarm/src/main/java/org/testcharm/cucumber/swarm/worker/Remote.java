package org.testcharm.cucumber.swarm.worker;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.testcharm.util.IterableBlockingQueue;
import org.testcharm.util.Sneaky;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class Remote {
    private static final Logger log = LoggerFactory.getLogger(Remote.class);

    public static Remote REMOTE;
    private final RestfulClient restfulClient;
    private final WorkerDataMapper dataMapper;
    private final int workerId;
    private final EventSerializer eventSerializer;
    private final IterableBlockingQueue<Object> eventQueue = new IterableBlockingQueue<>();
    private final Thread eventConsumerThread;

    public Remote(RestfulClient restfulClient, int workerId, WorkerDataMapper dataMapper) {
        this.restfulClient = restfulClient;
        this.workerId = workerId;
        this.dataMapper = dataMapper;
        eventSerializer = new EventSerializer(dataMapper);

        eventConsumerThread = new Thread(() -> {
            for (Object event : eventQueue) {
                String serialize = eventSerializer.serialize(event);
                log.debug(() -> String.format("Forwarding event: %s", serialize));
                restfulClient.httpPost(workerId, "/events", serialize);
            }
        });
        eventConsumerThread.start();
    }

    public static void setupRemote(RestfulClient restfulClient, int workerId, WorkerDataMapper dataMapper) {
        REMOTE = new Remote(restfulClient, workerId, dataMapper);
    }

    public void setupMapping(List<Feature> features) {
        features.forEach(dataMapper::mapGherkinFeature);
    }

    public Iterable<Pickle> pickles() {
        IterableBlockingQueue<Pickle> pickles = new IterableBlockingQueue<>(new ArrayBlockingQueue<>(1));
        new Thread(() -> {
            for (; ; ) {
                try {
                    log.debug(() -> "Requesting pickle...");
                    String pickleKey = restfulClient.httpGet(workerId, "/pickle");
                    pickles.put(dataMapper.pickle(pickleKey));
                    log.debug(() -> String.format("Received pickle<%s>", pickleKey));
                } catch (HttpException ig) {
                    log.debug(() -> "No pickle received");
                    pickles.close();
                }
            }
        }).start();
        return pickles;
    }

    public void sendEvent(Object event) {
        eventQueue.put(event);
    }

    public void exit() {
        eventQueue.close();
        Sneaky.run(eventConsumerThread::join);
    }

    public void sayReady() {
        restfulClient.httpPost(workerId, "/ready", "");
    }
}
