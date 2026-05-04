package org.testcharm.cucumber;

import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.options.RuntimeOptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.cucumber.swarm.ProcessedArgs;
import org.testcharm.cucumber.swarm.WorkerArgsPreProcessor;

import static org.testcharm.dal.Assertions.expect;

class WorkerArgsPreProcessorTest {
    WorkerArgsPreProcessor preProcessor = new WorkerArgsPreProcessor();
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    @Nested
    class WithLocalWorker {

        @Nested
        class ForceSingleThread {

            @Test
            void has_thread_arg() {
                String[] argv = {"--threads", "4", "features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(buildOptions(result.masterArgs)).should("threads: 1");
                expect(buildOptions(result.swarmArgs.getWorkerArgs())).should("threads: 1");
            }

            @Test
            void no_thread_arg() {
                String[] argv = {"features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(buildOptions(result.masterArgs)).should("threads: 1");
                expect(buildOptions(result.swarmArgs.getWorkerArgs())).should("threads: 1");
            }
        }

        @Nested
        class WorkerPluginsNoSummaryHasForwarding {

            @Test
            void has_no_summary_arg() {
                String[] argv = {"--no-summary", "features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(buildOptions(result.masterArgs)).should("plugins: []");
                expect(buildOptions(result.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}]");
            }

            @Test
            void no_no_summary_arg() {
                String[] argv = {"features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(buildOptions(result.masterArgs)).should("plugins: [{pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
                expect(buildOptions(result.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}]");
            }
        }

        @Nested
        class DisableAllOtherPluginsOnLocalWorker {

            @Test
            void disable_all_other_plugins_on_local_worker() {
                String[] argv = {"--plugin", "pretty", "features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(buildOptions(result.masterArgs)).should("plugins: [{pluginString: pretty} {pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
                expect(buildOptions(result.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}]");
            }
        }

        @Nested
        class SwarmPort {

            @Test
            void default_port() {
                String[] argv = {"features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(buildOptions(result.masterArgs)).should(": {...}");
                expect(buildOptions(result.swarmArgs.getWorkerArgs())).should(": {...}");

                expect(result.swarmArgs).should(": {swarmHost.port: 10083}");
            }

            @Test
            void set_port() {
                String[] argv = {"--swarm-port", "8000", "features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(buildOptions(result.masterArgs)).should(": {...}");
                expect(buildOptions(result.swarmArgs.getWorkerArgs())).should(": {...}");

                expect(result.swarmArgs).should(": {swarmHost.port: 8000}");
            }
        }
    }

    private RuntimeOptions buildOptions(String[] argv) {
        return new CommandlineOptionsParser(System.out).parse(argv)
                .addDefaultSummaryPrinterIfNotDisabled()
                .enablePublishPlugin()
                .build(RuntimeOptions.defaultOptions());
    }
}