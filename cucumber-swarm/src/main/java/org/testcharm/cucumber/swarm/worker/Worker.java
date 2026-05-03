package org.testcharm.cucumber.swarm.worker;

public interface Worker {
    Integer id();

    void start();

    void stop();
}
