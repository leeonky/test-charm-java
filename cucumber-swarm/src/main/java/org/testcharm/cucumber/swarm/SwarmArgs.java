package org.testcharm.cucumber.swarm;

import java.util.List;
import java.util.stream.Stream;

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
    private final String workingDir;
    private final List<String> targets;

    public SwarmArgs(String[] workerArgs, SwarmHost swarmHost, ClassLoader classLoader, boolean localWorker,
                     int remoteWorkerCount, List<String> remoteWorkerArgs, int workerTimeout, Integer workerId,
                     String workingDir, List<String> targets) {
        this.workerArgs = workerArgs;
        this.swarmHost = swarmHost;
        this.classLoader = classLoader;
        this.localWorker = localWorker;
        this.remoteWorkerCount = remoteWorkerCount;
        this.remoteWorkerArgs = remoteWorkerArgs;
        this.workerTimeout = workerTimeout;
        this.workerId = workerId;
        this.workingDir = workingDir;
        this.targets = targets;
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
        if (remoteWorkerArgs == null)
            throw new IllegalArgumentException("Missing remote worker args");
        return Stream.concat(remoteWorkerArgs.stream().map(s -> s.replace("{worker-id}", valueOf(index))),
                targets.stream()).toArray(String[]::new);
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

    public String getWorkingDir() {
        return workingDir;
    }

    public boolean isRemoteWorker() {
        return getWorkerId() != null;
    }
}
