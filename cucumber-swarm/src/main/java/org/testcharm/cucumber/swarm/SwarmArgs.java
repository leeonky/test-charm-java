package org.testcharm.cucumber.swarm;

import java.util.List;

import static java.lang.String.valueOf;

public class SwarmArgs {
    private final String[] workerArgs;
    private final SwarmHost swarmHost;
    private final ClassLoader classLoader;
    private final boolean localWorker;
    private final int remoteWorkerCount;
    private final List<String> remoteWorkerArgs;
    private final int workerTimeout;
    private final Integer workerId;

    public SwarmArgs(String[] workerArgs, SwarmHost swarmHost, ClassLoader classLoader, boolean localWorker,
                     int remoteWorkerCount, List<String> remoteWorkerArgs, int workerTimeout, Integer workerId) {
        this.workerArgs = workerArgs;
        this.swarmHost = swarmHost;
        this.classLoader = classLoader;
        this.localWorker = localWorker;
        this.remoteWorkerCount = remoteWorkerCount;
        this.remoteWorkerArgs = remoteWorkerArgs;
        this.workerTimeout = workerTimeout;
        this.workerId = workerId;
    }

    public String[] getWorkerArgs() {
        return workerArgs;
    }

    public SwarmHost getSwarmHost() {
        return swarmHost;
    }

    public ClassLoader classLoader() {
        return classLoader;
    }

    public boolean isLocalWorker() {
        return localWorker;
    }

    public String[] getRemoteWorkerArgs(int index) {
        return remoteWorkerArgs.stream().map(s -> s.replace("{worker-id}", valueOf(index))).toArray(String[]::new);
    }

    public int getRemoteWorkerCount() {
        return remoteWorkerCount;
    }

    public int getWorkerTimeout() {
        return workerTimeout;
    }

    public Integer getWorkerId() {
        return workerId;
    }
}
