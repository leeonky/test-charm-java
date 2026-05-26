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
        workerArgs.add("--no-summary");
        workerArgs.addAll(asList("--plugin", WorkerForwardingPlugin.class.getName()));

        SwarmHost swarmHost = new SwarmHost();
        boolean localWorker = true;
        int remoteWorkerCount = 0;
        int workerTimeout = 5;
        Integer workerId = null;
        String workingDir = System.getProperty("user.dir");

        String remoteLauncher = null;
        List<String> remoteWorkerArgs = new ArrayList<>();
        remoteWorkerArgs.addAll(asList("--threads", "1"));

        label:
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
                    String host = args.removeFirst();
                    swarmHost.setHost(host);
                    remoteWorkerArgs.addAll(asList("--swarm-host", host));
                    break;
                case "--swarm-port":
                    int port = popIntValue(args);
                    swarmHost.setPort(port);
                    remoteWorkerArgs.addAll(asList("--swarm-port", String.valueOf(port)));
                    break;
                case "--threads":
                    args.removeFirst(); // ignore thread force to 1
                    break;


                case "--plugin":
                    String plugin = args.removeFirst();
                    masterArgs.add(arg);
                    masterArgs.add(plugin);
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
                case "--working-dir":
                    workingDir = args.removeFirst();
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

        List<String> remoteWorkerLauncherAndArgs = null;
        if (remoteLauncher != null) {
            remoteWorkerLauncherAndArgs = new ArrayList<>();
            remoteWorkerLauncherAndArgs.add(remoteLauncher);
            remoteWorkerLauncherAndArgs.addAll(remoteWorkerArgs);
        }
        return new ProcessedArgs(masterArgs.toArray(new String[0]),
                new SwarmArgs(workerArgs.toArray(new String[0]), swarmHost, classLoader, localWorker,
                        remoteWorkerCount, remoteWorkerLauncherAndArgs, workerTimeout, workerId, workingDir));
    }

    private int popIntValue(LinkedList<String> args) {
        return parseInt(args.removeFirst());
    }
}
