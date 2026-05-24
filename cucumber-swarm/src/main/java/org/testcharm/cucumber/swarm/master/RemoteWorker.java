package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.testcharm.cucumber.swarm.SwarmArgs;
import org.testcharm.util.Sneaky;

import static org.testcharm.cucumber.swarm.util.IoUtil.readAll;

public class RemoteWorker extends AbstractWorker {
    private final Process process;
    private final Logger log = LoggerFactory.getLogger(RemoteWorker.class);

    public RemoteWorker(SwarmArgs swarmArgs) {
        log.info(() -> String.format("Remote worker<%d> starting...", id));
        process = Sneaky.get(() -> new ProcessBuilder(swarmArgs.getRemoteWorkerArgs(id())).start());
    }

    @Override
    public void shutdown() {
        int code = Sneaky.get(process::waitFor);
        if (code == 0)
            log.info(() -> String.format("Worker<%d> exit(%d)", id, code));
        else
            log.info(() -> String.format("Worker<%d> exit(%d)\n%s", id, code,
                    new String(readAll(process.getErrorStream()))));
    }
}
