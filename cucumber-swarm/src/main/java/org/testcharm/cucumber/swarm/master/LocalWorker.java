package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.runtime.WorkerRuntime;
import org.testcharm.cucumber.swarm.Main;
import org.testcharm.cucumber.swarm.SwarmArgs;
import org.testcharm.util.Sneaky;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.testcharm.cucumber.swarm.Main.buildRuntimeOption;

public class LocalWorker extends AbstractWorker {
    private static final Logger log = LoggerFactory.getLogger(LocalWorker.class);
    private final CompletableFuture<Byte> future;

    public LocalWorker(SwarmArgs swarmArgs) {
        log.info(() -> String.format("Local worker<%d> starting...", id()));
        future = CompletableFuture.supplyAsync(() -> {
            Main.Result worker = buildRuntimeOption(swarmArgs.getWorkerArgs());

            Optional<Byte> exitStatus = worker.commandlineOptionsParser.exitStatus();
            if (exitStatus.isPresent()) {
                return exitStatus.get();
            }

            final WorkerRuntime workerRuntime = WorkerRuntime.builder()
                    .withRuntimeOptions(worker.runtimeOptions)
                    .withClassLoader(swarmArgs::classLoader)
                    .build(id(), swarmArgs);

            workerRuntime.run();
            return workerRuntime.exitStatus();
        });
    }

    @Override
    public void shutdown() {
        try {
            byte result = future.join();
            log.info(() -> String.format("Worker<%d> exit(%d)", id(), result));
        } catch (CompletionException e) {
//            TODO need test
            Sneaky.sneakyThrow(e.getCause());
        }
    }

    @Override
    public int id() {
        return 0;
    }
}
