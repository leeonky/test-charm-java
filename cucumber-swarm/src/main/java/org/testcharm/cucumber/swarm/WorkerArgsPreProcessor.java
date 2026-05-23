package org.testcharm.cucumber.swarm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Integer.parseInt;

public class WorkerArgsPreProcessor {
    public ProcessedArgs process(String[] argv, ClassLoader classLoader) {
        LinkedList<String> args = new LinkedList<>(Arrays.asList(argv));
        List<String> masterArgs = new ArrayList<>();
        masterArgs.add("--threads");
        masterArgs.add("1");

        masterArgs.add("--plugin");
        masterArgs.add(MasterPlugin.class.getName());

        List<String> workerArgs = new ArrayList<>();
        workerArgs.add("--threads");
        workerArgs.add("1");
        workerArgs.add("--no-summary");
        workerArgs.add("--plugin");
        workerArgs.add("org.testcharm.cucumber.swarm.WorkerForwardingPlugin");

        SwarmHost swarmHost = new SwarmHost();
        boolean localWorker = true;
        int remoteWorkerCount = 1;

        while (!args.isEmpty()) {
            String arg = args.removeFirst();
            if (arg.equals("--threads")) {
                args.removeFirst(); // ignore thread force to 1
            } else if (arg.equals("--plugin")) {
                String plugin = args.removeFirst();
                masterArgs.add(arg);
                masterArgs.add(plugin);
            } else if (arg.equals("--swarm-port")) {
                swarmHost.setPort(parseInt(args.removeFirst()));
            } else if (arg.equals("--local-worker")) {
                String mode = args.removeFirst();
                localWorker = mode.equalsIgnoreCase("enable");
            } else if (arg.equals("--remote-worker-count")) {
                remoteWorkerCount = parseInt(args.removeFirst());
            } else if (arg.equals("--")) {
                break;
            } else {
                masterArgs.add(arg);
                workerArgs.add(arg);
            }
        }
        return new ProcessedArgs(masterArgs.toArray(new String[0]),
                new SwarmArgs(workerArgs.toArray(new String[0]), swarmHost, classLoader, localWorker, remoteWorkerCount, args));
    }
}
