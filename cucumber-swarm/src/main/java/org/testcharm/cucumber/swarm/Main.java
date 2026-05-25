package org.testcharm.cucumber.swarm;

import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.MasterRuntime;
import io.cucumber.core.runtime.WorkerRuntime;

import java.util.Optional;


public class Main {

    public static void main(String... argv) {
        System.exit(run(argv));
    }

    public static byte run(String... argv) {
        return run(argv, Thread.currentThread().getContextClassLoader());
    }

    public static byte run(String[] argv, ClassLoader classLoader) {
        ProcessedArgs argvs = new WorkerArgsPreProcessor().process(argv, classLoader);
        if (argvs.swarmArgs.getWorkerId() != null) {
            Main.Result worker = buildRuntimeOption(argvs.swarmArgs.getWorkerArgs());

            Optional<Byte> exitStatus = worker.commandlineOptionsParser.exitStatus();
            if (exitStatus.isPresent()) {
                return exitStatus.get();
            }

            final WorkerRuntime workerRuntime = WorkerRuntime.builder()
                    .withRuntimeOptions(worker.runtimeOptions)
                    .withClassLoader(() -> classLoader)
                    .build(argvs.swarmArgs.getWorkerId(), argvs.swarmArgs);

            workerRuntime.run();
            return workerRuntime.exitStatus();
        } else {
            Result master = buildRuntimeOption(argvs.masterArgs);

            Optional<Byte> exitStatus = master.commandlineOptionsParser.exitStatus();
            if (exitStatus.isPresent()) {
                return exitStatus.get();
            }

            final MasterRuntime masterRuntime = MasterRuntime.builder()
                    .withRuntimeOptions(master.runtimeOptions)
                    .withClassLoader(() -> classLoader)
                    .build(argvs.swarmArgs);

            masterRuntime.run();
            return masterRuntime.exitStatus();
        }
    }

    public static Result buildRuntimeOption(String[] argv) {
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

    public static class Result {
        public final CommandlineOptionsParser commandlineOptionsParser;
        public final RuntimeOptions runtimeOptions;

        public Result(CommandlineOptionsParser commandlineOptionsParser, RuntimeOptions runtimeOptions) {
            this.commandlineOptionsParser = commandlineOptionsParser;
            this.runtimeOptions = runtimeOptions;
        }
    }
}
