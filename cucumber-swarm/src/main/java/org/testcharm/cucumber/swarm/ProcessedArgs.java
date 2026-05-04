package org.testcharm.cucumber.swarm;

public class ProcessedArgs {
    public final String[] masterArgs;
    public final SwarmArgs swarmArgs;

    public ProcessedArgs(String[] masterArgs, SwarmArgs swarmArgs) {
        this.masterArgs = masterArgs;
        this.swarmArgs = swarmArgs;
    }
}
