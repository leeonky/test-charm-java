package org.testcharm.cucumber.swarm;

import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.MasterRuntime;
import io.cucumber.core.runtime.WorkerRuntime;
import org.testcharm.util.Pair;
import org.testcharm.util.Sneaky;

import java.util.Optional;


public class Main {

    public static void main(String... argv) {
        System.exit(run(argv));
    }

    public static byte run(String... argv) {
        return run(argv, Thread.currentThread().getContextClassLoader());
    }

    public static byte run(String[] argv, ClassLoader classLoader) {
        Pair<String[], String[]> argvs = new WorkerArgsPreProcessor().process(argv);
        Result master = buildRuntimeOption(argvs.getFirst());

        Optional<Byte> exitStatus = master.commandlineOptionsParser.exitStatus();
        if (exitStatus.isPresent()) {
            return exitStatus.get();
        }

        final MasterRuntime masterRuntime = MasterRuntime.builder()
                .withRuntimeOptions(master.runtimeOptions)
                .withClassLoader(() -> classLoader)
                .build();

        Result worker = buildRuntimeOption(argvs.getSecond());

        exitStatus = worker.commandlineOptionsParser.exitStatus();
        if (exitStatus.isPresent()) {
            return exitStatus.get();
        }

        final WorkerRuntime workerRuntime = WorkerRuntime.builder()
                .withRuntimeOptions(worker.runtimeOptions)
                .withClassLoader(() -> classLoader)
                .build(masterRuntime.scheduler().server());

        Thread thread = new Thread(workerRuntime::run);
        thread.start();

        masterRuntime.run();

        Sneaky.run(thread::join);
        return masterRuntime.exitStatus();
    }

    private static Result buildRuntimeOption(String[] argv) {
        RuntimeOptions propertiesFileOptions = new CucumberPropertiesParser()
                .parse(CucumberProperties.fromPropertiesFile())
                .build();

        RuntimeOptions environmentOptions = new CucumberPropertiesParser()
                .parse(CucumberProperties.fromEnvironment())
                .build(propertiesFileOptions);

        RuntimeOptions systemOptions = new CucumberPropertiesParser()
                .parse(CucumberProperties.fromSystemProperties())
                .build(environmentOptions);

        CommandlineOptionsParser commandlineOptionsParser = new CommandlineOptionsParser(System.out);
        RuntimeOptions runtimeOptions = commandlineOptionsParser
                .parse(argv)
                .addDefaultGlueIfAbsent()
                .addDefaultFeaturePathIfAbsent()
                .addDefaultSummaryPrinterIfNotDisabled()
                .enablePublishPlugin()
                .build(systemOptions);
        return new Result(commandlineOptionsParser, runtimeOptions);
    }

    private static class Result {
        public final CommandlineOptionsParser commandlineOptionsParser;
        public final RuntimeOptions runtimeOptions;

        public Result(CommandlineOptionsParser commandlineOptionsParser, RuntimeOptions runtimeOptions) {
            this.commandlineOptionsParser = commandlineOptionsParser;
            this.runtimeOptions = runtimeOptions;
        }
    }
}
