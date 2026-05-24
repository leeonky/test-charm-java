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
        class SwarmMode {

            @Test
            void default_local_worker() {
                String[] argv = {"features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(result.swarmArgs).should("localWorker: true");
            }

            @Test
            void enable_local_worker() {
                String[] argv = {"--local-worker", "enable", "features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(result.swarmArgs).should("localWorker: true");
            }
        }

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

                expect(buildOptions(result.masterArgs)).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.MasterPlugin}]");
                expect(buildOptions(result.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}]");
            }

            @Test
            void no_no_summary_arg() {
                String[] argv = {"features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(buildOptions(result.masterArgs)).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.MasterPlugin}" +
                        "{pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
                expect(buildOptions(result.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}]");
            }
        }

        @Nested
        class DisableAllOtherPluginsOnLocalWorker {

            @Test
            void disable_all_other_plugins_on_local_worker() {
                String[] argv = {"--plugin", "pretty", "features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(buildOptions(result.masterArgs)).should("plugins: [" +
                        "{pluginString: org.testcharm.cucumber.swarm.MasterPlugin} " +
                        "{pluginString: pretty} " +
                        "{pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter} " +
                        "]");
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

        @Nested
        class WorkerTimeout {

            @Test
            void default_worker_timeout() {
                String[] argv = {"features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(result.swarmArgs).should("workerTimeout: 5");
            }

            @Test
            void worker_timeout() {
                String[] argv = {"--worker-timeout", "120", "features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(result.swarmArgs).should("workerTimeout: 120");
            }
        }
    }

    @Nested
    class WithRemoteWorker {

        @Nested
        class SwarmMode {

            @Test
            void disable_local_worker() {
                String[] argv = {"--local-worker", "disable", "features", "--", "ls"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(result.swarmArgs).should("localWorker: false");
            }
        }

        @Nested
        class RemoteWorkerCount {

            @Test
            void default_remote_worker_count() {
                String[] argv = {"features", "--", "ls"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(result.swarmArgs).should("remoteWorkerCount: 0");
            }

            @Test
            void specify_remote_worker_count() {
                String[] argv = {"--remote-worker-count", "5", "features", "--", "ls"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(result.swarmArgs).should("remoteWorkerCount: 5");
            }
        }

        @Nested
        class RemoteWorkerArgs {

            @Test
            void no_remote_worker_args() {
                String[] argv = {"features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(result.swarmArgs.getRemoteWorkerArgs(43)).should("= []");
            }

            @Test
            void remote_worker_args() {
                String[] argv = {"features", "--", "ls", "{worker-id}"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(result.swarmArgs.getRemoteWorkerArgs(43)).should(": [ls, '43']");
            }
        }

        @Nested
        class RemoteSite {

            @Test
            void master() {
                String[] argv = {"features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(result.swarmArgs).should("workerId: null");
            }

            @Test
            void worker() {
                String[] argv = {"--worker-id", "100", "features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(result.swarmArgs).should("workerId: 100");
            }
        }

        @Nested
        class NoSummary {

            @Test
            void no_no_summary_arg() {
                String[] argv = {"--worker-id", "100", "features"};
                ProcessedArgs result = preProcessor.process(argv, classLoader);

                expect(buildOptions(result.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}" +
                        "{pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
            }
        }
    }

    private RuntimeOptions buildOptions(String[] argv) {
        return new CommandlineOptionsParser(System.out).parse(argv)
                .addDefaultSummaryPrinterIfNotDisabled()
                .enablePublishPlugin()
                .build(RuntimeOptions.defaultOptions());
    }

//    remote command
}