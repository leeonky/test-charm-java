package org.testcharm.cucumber.swarm;

import org.testcharm.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class WorkerArgsPreProcessor {
    public Pair<String[], String[]> process(String[] argv) {
        LinkedList<String> args = new LinkedList<>(Arrays.asList(argv));
        List<String> masterArgs = new ArrayList<>();
        masterArgs.add("--threads");
        masterArgs.add("1");

        List<String> workerArgs = new ArrayList<>();
        workerArgs.add("--threads");
        workerArgs.add("1");
        workerArgs.add("--no-summary");
        workerArgs.add("--plugin");
        workerArgs.add("org.testcharm.cucumber.swarm.WorkerForwardingPlugin");

        while (!args.isEmpty()) {
            String arg = args.removeFirst();
            if (arg.equals("--threads")) {
                args.removeFirst(); // ignore thread force to 1
            } else if (arg.equals("--plugin")) {
                String plugin = args.removeFirst();
                masterArgs.add(arg);
                masterArgs.add(plugin);
            } else {
                masterArgs.add(arg);
                workerArgs.add(arg);
            }
        }

        return new Pair<>(masterArgs.toArray(new String[0]), workerArgs.toArray(new String[0]));
    }
}
