package org.testcharm.cucumber.swarm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;

public class WorkerArgsPreProcessor {
    public ProcessedArgs process(String[] argv, ClassLoader classLoader) {
        LinkedList<String> args = new LinkedList<>(asList(argv));
        List<String> masterArgs = new ArrayList<>();
        masterArgs.addAll(asList("--threads", "1"));
        masterArgs.addAll(asList("--plugin", MasterPlugin.class.getName()));

        List<String> workerArgs = new ArrayList<>();
        workerArgs.addAll(asList("--threads", "1"));
        workerArgs.addAll(asList("--plugin", WorkerForwardingPlugin.class.getName()));

        SwarmHost swarmHost = new SwarmHost();
        boolean localWorker = true;
        int remoteWorkerCount = 0;
        int workerTimeout = 5;
        Integer workerId = null;
        String swarmHostStr = null;
        String workingDir = System.getProperty("user.dir");

        String remoteLauncher = null;
        List<String> remoteWorkerArgs = new ArrayList<>();
        remoteWorkerArgs.addAll(asList("--threads", "1"));
        remoteWorkerArgs.addAll(asList("--worker-id", "{worker-id}"));

        List<String> targets = new ArrayList<>();

        while (!args.isEmpty()) {
            String arg = args.removeFirst();
            switch (arg) {
                case "--remote-worker-launcher":
                    remoteLauncher = args.removeFirst();
                    break;
                case "--disable-local-worker":
                    localWorker = false;
                    break;
                case "--swarm-host":
                    swarmHostStr = args.removeFirst();
                    break;
                case "--swarm-port":
                    int port = popIntValue(args);
                    swarmHost.setPort(port);
                    remoteWorkerArgs.addAll(asList("--swarm-port", String.valueOf(port)));
                    break;
                case "--threads":
                    args.removeFirst(); // ignore thread force to 1
                    break;
                case "--worker-timeout":
                    workerTimeout = popIntValue(args);
                    break;
                case "--plugin":
                    String plugin = args.removeFirst();
                    masterArgs.add(arg);
                    masterArgs.add(plugin);
                    break;
                case "--no-summary":
                    masterArgs.add(arg);
                    break;
                case "--working-dir":
                    workingDir = args.removeFirst();
                    break;
                case "--remote-worker-count":
                    remoteWorkerCount = popIntValue(args);
                    break;
                case "--remote-working-dir":
                    remoteWorkerArgs.addAll(asList("--working-dir", args.removeFirst()));
                    break;
                case "--worker-id":
                    workerId = popIntValue(args);
                    break;
                default:
                    masterArgs.add(arg);
                    workerArgs.add(arg);
                    targets.add(arg);
                    break;
            }
        }

        if (workerId != null) {
            if (swarmHostStr != null)
                swarmHost.setHost(swarmHostStr);
        } else {
            workerArgs.add(0, "--no-summary");
            if (swarmHostStr != null)
                remoteWorkerArgs.addAll(asList("--swarm-host", swarmHostStr));
        }

        List<String> remoteWorkerLauncherAndArgs = null;
        if (remoteLauncher != null) {
            remoteWorkerLauncherAndArgs = new ArrayList<>();
            remoteWorkerLauncherAndArgs.add(remoteLauncher);
            remoteWorkerLauncherAndArgs.addAll(remoteWorkerArgs);
        }
        return new ProcessedArgs(masterArgs.toArray(new String[0]),
                new SwarmArgs(workerArgs.toArray(new String[0]), swarmHost, classLoader, localWorker,
                        remoteWorkerCount, remoteWorkerLauncherAndArgs, workerTimeout, workerId, workingDir, targets));
    }

    private int popIntValue(LinkedList<String> args) {
        return parseInt(args.removeFirst());
    }
}
