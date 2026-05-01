package org.testcharm.cucumber.swarm;

public class ProcessedArgs {
    public final String[] masterArgs;
    public final String[] workerArgs;

    public ProcessedArgs(String[] masterArgs, String[] workerArgs) {
        this.masterArgs = masterArgs;
        this.workerArgs = workerArgs;
    }
}
