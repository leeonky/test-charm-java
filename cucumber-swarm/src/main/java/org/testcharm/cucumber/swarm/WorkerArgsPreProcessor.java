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
        int remoteWorkerCount = 0;
        int workerTimeout = 5;
        Integer workerId = null;

        label:
        while (!args.isEmpty()) {
            String arg = args.removeFirst();
            switch (arg) {
                case "--threads":
                    args.removeFirst(); // ignore thread force to 1
                    break;
                case "--plugin":
                    String plugin = args.removeFirst();
                    masterArgs.add(arg);
                    masterArgs.add(plugin);
                    break;
                case "--swarm-port":
                    swarmHost.setPort(popIntValue(args));
                    break;
                case "--local-worker":
                    String mode = args.removeFirst();
                    localWorker = mode.equalsIgnoreCase("enable");
                    break;
                case "--remote-worker-count":
                    remoteWorkerCount = popIntValue(args);
                    break;
                case "--worker-timeout":
                    workerTimeout = popIntValue(args);
                    break;
                case "--worker-id":
                    workerId = popIntValue(args);
                    break;
                case "--":
                    break label;
                default:
                    masterArgs.add(arg);
                    workerArgs.add(arg);
                    break;
            }
        }
        if (workerId != null)
            workerArgs.remove("--no-summary");
        return new ProcessedArgs(masterArgs.toArray(new String[0]),
                new SwarmArgs(workerArgs.toArray(new String[0]), swarmHost, classLoader, localWorker,
                        remoteWorkerCount, args, workerTimeout, workerId));
    }

    private int popIntValue(LinkedList<String> args) {
        return parseInt(args.removeFirst());
    }
}
