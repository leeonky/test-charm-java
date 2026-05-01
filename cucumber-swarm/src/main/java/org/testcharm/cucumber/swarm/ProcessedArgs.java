package org.testcharm.cucumber.swarm;

public class ProcessedArgs {
    public final String[] masterArgs;
    public final String[] workerArgs;
    public final SwarmArg swarmArgs;

    public ProcessedArgs(String[] masterArgs, String[] workerArgs, SwarmArg swarmArgs) {
        this.masterArgs = masterArgs;
        this.workerArgs = workerArgs;
        this.swarmArgs = swarmArgs;
    }
}
