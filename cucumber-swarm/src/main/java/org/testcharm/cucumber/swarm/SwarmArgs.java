package org.testcharm.cucumber.swarm;

public class SwarmArgs {
    private final String[] workerArgs;
    private final SwarmHost swarmHost;
    private final ClassLoader classLoader;
    private int workerId;

    public SwarmArgs(String[] workerArgs, SwarmHost swarmHost, ClassLoader classLoader) {
        this.workerArgs = workerArgs;
        this.swarmHost = swarmHost;
        this.classLoader = classLoader;
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

    public int getWorkerId() {
        return workerId;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }
}
