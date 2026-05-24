package org.testcharm.cucumber.swarm;

import java.util.List;

import static java.lang.String.valueOf;

public class SwarmArgs {
    private final String[] localWorkerArgs;
    private final SwarmHost swarmHost;
    private final ClassLoader classLoader;
    private int workerId;
    private final boolean localWorker;
    private final int remoteWorkerCount;
    private final List<String> remoteWorkerArgs;
    private final int workerTimeout;

    public SwarmArgs(String[] localWorkerArgs, SwarmHost swarmHost, ClassLoader classLoader, boolean localWorker,
                     int remoteWorkerCount, List<String> remoteWorkerArgs, int workerTimeout) {
        this.localWorkerArgs = localWorkerArgs;
        this.swarmHost = swarmHost;
        this.classLoader = classLoader;
        this.localWorker = localWorker;
        this.remoteWorkerCount = remoteWorkerCount;
        this.remoteWorkerArgs = remoteWorkerArgs;
        this.workerTimeout = workerTimeout;
    }

    public String[] getLocalWorkerArgs() {
        return localWorkerArgs;
    }

    public SwarmHost getSwarmHost() {
        return swarmHost;
    }

    public ClassLoader classLoader() {
        return classLoader;
    }

    public int getWorkerId() {
        return workerId;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }

    public boolean isLocalWorker() {
        return localWorker;
    }

    public String[] getRemoteWorkerArgs(int index) {
        return remoteWorkerArgs.stream().map(s -> s.replace("{remote-worker-index}", valueOf(index))).toArray(String[]::new);
    }

    public int getRemoteWorkerCount() {
        return remoteWorkerCount;
    }

    public int getWorkerTimeout() {
        return workerTimeout;
    }
}
