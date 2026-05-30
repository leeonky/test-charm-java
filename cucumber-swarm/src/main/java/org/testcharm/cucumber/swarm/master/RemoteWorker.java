package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.testcharm.cucumber.swarm.SwarmArgs;
import org.testcharm.util.Sneaky;

import java.util.Arrays;

import static org.testcharm.cucumber.swarm.util.IoUtil.readAll;

public class RemoteWorker extends AbstractWorker {
    private static final Logger log = LoggerFactory.getLogger(RemoteWorker.class);
    private final int id = Worker.ID_GENERATOR.incrementAndGet();
    private final Process process;

    public RemoteWorker(SwarmArgs swarmArgs) {
        String[] remoteWorkerArgs = swarmArgs.getRemoteWorkerArgs(id());
        log.info(() -> String.format("Remote worker<%d> starting with <%s>...", id, Arrays.toString(remoteWorkerArgs)));
        process = Sneaky.get(() -> new ProcessBuilder(remoteWorkerArgs).start());
    }

    @Override
    public int id() {
        return id;
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
