package org.testcharm.cucumber;

import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.options.RuntimeOptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.cucumber.swarm.ProcessedArgs;
import org.testcharm.cucumber.swarm.WorkerArgsPreProcessor;

import static org.testcharm.dal.Assertions.expect;
import static org.testcharm.dal.Assertions.expectRun;

class WorkerArgsPreProcessorTest {
    WorkerArgsPreProcessor preProcessor = new WorkerArgsPreProcessor();
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    @Nested
    class OptionRemoteWorkerLauncher {

        @Nested
        class DefaultArg {
            private final String[] argv = {"features"};
            private final ProcessedArgs master = preProcessor.process(argv, classLoader);

            @Test
            void should_contains_in_args() {
                expect(master.masterArgs).should(": [... features]");
                expect(master.swarmArgs.getWorkerArgs()).should(": [... features]");
            }

            @Test
            void build_option_with_no_error() {
                expect(buildOptions(master.masterArgs)).should(": {...}");
                expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
            }

            @Test
            void raise_error_when_get_remote_worker_args() {
                expectRun(() -> master.swarmArgs.getRemoteWorkerArgs(1)).should("::throw.class.simpleName= IllegalArgumentException");
            }
        }

        @Nested
        class SpecifyArg {
            private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "features"};
            private final ProcessedArgs master = preProcessor.process(argv, classLoader);

            @Test
            void should_not_contains_in_args() {
                expect(master.masterArgs).should("::should::not.contains: '--remote-worker-launcher'");
                expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--remote-worker-launcher'");
            }

            @Test
            void build_option_with_no_error() {
                expect(buildOptions(master.masterArgs)).should(": {...}");
                expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
            }

            @Nested
            class RemoteArgs {

                private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
                private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);

                @Test
                void specify_remote_worker_launcher_as_execute_bin() {
                    expect(remoteArgs).should(": [run-cucumber.sh ...]");
                }

                @Test
                void build_option_with_no_error() {
                    expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should(": {...}");
                }
            }
        }
    }

    @Nested
    class FlagDisableLocalWorker {

        @Nested
        class WithoutRemoteWorker {

            @Nested
            class DefaultArg {
                private final String[] argv = {"features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--disable-local-worker'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--disable-local-worker'");
                }

                @Test
                void default_enable_local_worker() {
                    expect(master.swarmArgs).should("localWorker: true");
                }

                @Test
                void build_option_with_no_error() {
                    expect(buildOptions(master.masterArgs)).should(": {...}");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
                }

                @Test
                void raise_error_when_get_remote_worker_args() {
                    expectRun(() -> master.swarmArgs.getRemoteWorkerArgs(1)).should("::throw.class.simpleName= IllegalArgumentException");
                }
            }

            @Nested
            class SpecifyArg {
                private final String[] argv = {"--disable-local-worker", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--disable-local-worker'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--disable-local-worker'");
                }

                @Test
                void default_enable_local_worker() {
                    expect(master.swarmArgs).should("localWorker: false");
                }

                @Test
                void build_option_with_no_error() {
                    expect(buildOptions(master.masterArgs)).should(": {...}");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
                }

                @Test
                void raise_error_when_get_remote_worker_args() {
                    expectRun(() -> master.swarmArgs.getRemoteWorkerArgs(1)).should("::throw.class.simpleName= IllegalArgumentException");
                }
            }
        }

        @Nested
        class WithRemoteWorker {

            @Nested
            class DefaultArg {
                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--disable-local-worker'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--disable-local-worker'");
                }

                @Test
                void default_enable_local_worker() {
                    expect(master.swarmArgs).should("localWorker: true");
                }

                @Test
                void build_option_with_no_error() {
                    expect(buildOptions(master.masterArgs)).should(": {...}");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
                }

                @Nested
                class RemoteArgs {

                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
                    private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);

                    @Test
                    void should_not_contains_in_remote_worker_args() {
                        expect(remoteArgs).should("::should::not.contains: '--disable-local-worker'");
                    }

                    @Test
                    void build_option_with_no_error() {
                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should(": {...}");
                    }

                    @Test
                    void default_enable_local_worker() {
                        // not use in remote worker
                        expect(remote.swarmArgs).should("localWorker: true");
                    }
                }
            }

            @Nested
            class SpecifyArg {
                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "--disable-local-worker", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--disable-local-worker'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--disable-local-worker'");
                }

                @Test
                void default_enable_local_worker() {
                    expect(master.swarmArgs).should("localWorker: false");
                }

                @Test
                void build_option_with_no_error() {
                    expect(buildOptions(master.masterArgs)).should(": {...}");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
                }

                @Nested
                class RemoteArgs {

                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
                    private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);

                    @Test
                    void should_not_contains_in_remote_worker_args() {
                        expect(remoteArgs).should("::should::not.contains: '--disable-local-worker'");
                    }

                    @Test
                    void build_option_with_no_error() {
                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should(": {...}");
                    }

                    @Test
                    void default_enable_local_worker() {
                        // not use in remote worker
                        expect(remote.swarmArgs).should("localWorker: true");
                    }
                }
            }
        }
    }

    @Nested
    class OptionSwarmHost {

        @Nested
        class WithoutRemoteWorker {

            @Nested
            class DefaultArg {
                private final String[] argv = {"features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--swarm-host'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--swarm-host'");
                }

                @Test
                void default_swarm_host() {
                    expect(master.swarmArgs).should("swarmHost.host= localhost");
                }

                @Test
                void build_option_with_no_error() {
                    expect(buildOptions(master.masterArgs)).should(": {...}");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
                }

                @Test
                void raise_error_when_get_remote_worker_args() {
                    expectRun(() -> master.swarmArgs.getRemoteWorkerArgs(1)).should("::throw.class.simpleName= IllegalArgumentException");
                }
            }

            @Nested
            class SpecifyArg {
                private final String[] argv = {"--swarm-host", "192.168.110.100", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--swarm-host'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--swarm-host'");
                }

                @Test
                void default_swarm_host() {
                    expect(master.swarmArgs).should("swarmHost.host= '192.168.110.100'");
                }

                @Test
                void build_option_with_no_error() {
                    expect(buildOptions(master.masterArgs)).should(": {...}");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
                }

                @Test
                void raise_error_when_get_remote_worker_args() {
                    expectRun(() -> master.swarmArgs.getRemoteWorkerArgs(1)).should("::throw.class.simpleName= IllegalArgumentException");
                }
            }
        }

        @Nested
        class WithRemoteWorker {

            @Nested
            class DefaultArg {
                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--swarm-host'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--swarm-host'");
                }

                @Test
                void default_swarm_host() {
                    expect(master.swarmArgs).should("swarmHost.host= localhost");
                }

                @Test
                void build_option_with_no_error() {
                    expect(buildOptions(master.masterArgs)).should(": {...}");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
                }

                @Nested
                class RemoteArgs {

                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
                    private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);

                    @Test
                    void should_not_contains_in_remote_worker_args() {
                        expect(remoteArgs).should("::should::not.contains: '--swarm-host'");
                    }

                    @Test
                    void build_option_with_no_error() {
                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should(": {...}");
                    }

                    @Test
                    void default_swarm_host() {
                        expect(remote.swarmArgs).should("swarmHost.host= localhost");
                    }
                }
            }

            @Nested
            class SpecifyArg {
                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "--swarm-host", "192.168.110.100", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--swarm-host'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--swarm-host'");
                }

                @Test
                void default_swarm_host() {
                    expect(master.swarmArgs).should("swarmHost.host= '192.168.110.100'");
                }

                @Test
                void build_option_with_no_error() {
                    expect(buildOptions(master.masterArgs)).should(": {...}");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
                }

                @Nested
                class RemoteArgs {

                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
                    private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);

                    @Test
                    void should_not_contains_in_remote_worker_args() {
                        expect(remoteArgs).should(": [... '--swarm-host' '192.168.110.100' ...]");
                    }

                    @Test
                    void build_option_with_no_error() {
                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should(": {...}");
                    }

                    @Test
                    void default_swarm_host() {
                        expect(remote.swarmArgs).should("swarmHost.host= '192.168.110.100'");
                    }
                }
            }
        }
    }

    @Nested
    class OptionSwarmPort {

        @Nested
        class WithoutRemoteWorker {

            @Nested
            class DefaultArg {
                private final String[] argv = {"features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--swarm-port'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--swarm-port'");
                }

                @Test
                void default_swarm_port() {
                    expect(master.swarmArgs).should("swarmHost.port= 10083");
                }

                @Test
                void build_option_with_no_error() {
                    expect(buildOptions(master.masterArgs)).should(": {...}");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
                }

                @Test
                void raise_error_when_get_remote_worker_args() {
                    expectRun(() -> master.swarmArgs.getRemoteWorkerArgs(1)).should("::throw.class.simpleName= IllegalArgumentException");
                }
            }

            @Nested
            class SpecifyArg {
                private final String[] argv = {"--swarm-port", "20000", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--swarm-port'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--swarm-port'");
                }

                @Test
                void default_swarm_port() {
                    expect(master.swarmArgs).should("swarmHost.port= 20000");
                }

                @Test
                void build_option_with_no_error() {
                    expect(buildOptions(master.masterArgs)).should(": {...}");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
                }

                @Test
                void raise_error_when_get_remote_worker_args() {
                    expectRun(() -> master.swarmArgs.getRemoteWorkerArgs(1)).should("::throw.class.simpleName= IllegalArgumentException");
                }
            }
        }

        @Nested
        class WithRemoteWorker {

            @Nested
            class DefaultArg {
                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--swarm-port'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--swarm-port'");
                }

                @Test
                void default_swarm_port() {
                    expect(master.swarmArgs).should("swarmHost.port= 10083");
                }

                @Test
                void build_option_with_no_error() {
                    expect(buildOptions(master.masterArgs)).should(": {...}");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
                }

                @Nested
                class RemoteArgs {

                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
                    private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);

                    @Test
                    void should_not_contains_in_remote_worker_args() {
                        expect(remoteArgs).should("::should::not.contains: '--swarm-port'");
                    }

                    @Test
                    void build_option_with_no_error() {
                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should(": {...}");
                    }

                    @Test
                    void default_swarm_part() {
                        expect(remote.swarmArgs).should("swarmHost.port= 10083");
                    }
                }
            }

            @Nested
            class SpecifyArg {
                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "--swarm-port", "20000", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--swarm-port'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--swarm-port'");
                }

                @Test
                void default_swarm_port() {
                    expect(master.swarmArgs).should("swarmHost.port= 20000");
                }

                @Test
                void build_option_with_no_error() {
                    expect(buildOptions(master.masterArgs)).should(": {...}");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
                }

                @Nested
                class RemoteArgs {

                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
                    private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);

                    @Test
                    void should_not_contains_in_remote_worker_args() {
                        expect(remoteArgs).should(": [... '--swarm-port' '20000' ...]");
                    }

                    @Test
                    void build_option_with_no_error() {
                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should(": {...}");
                    }

                    @Test
                    void default_swarm_port() {
                        expect(remote.swarmArgs).should("swarmHost.port= 20000");
                    }
                }
            }
        }
    }

    @Nested
    class OptionThread {

        @Nested
        class WithoutRemoteWorker {

            @Nested
            class DefaultArg {
                private final String[] argv = {"features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_contains_threads_1_in_args() {
                    expect(master.masterArgs).should(": [... '--threads', '1' ...]");
                    expect(master.swarmArgs.getWorkerArgs()).should(": [... '--threads', '1' ...]");
                }

                @Test
                void force_threads_to_1() {
                    expect(buildOptions(master.masterArgs)).should("threads: 1");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should("threads: 1");
                }

                @Test
                void raise_error_when_get_remote_worker_args() {
                    expectRun(() -> master.swarmArgs.getRemoteWorkerArgs(1)).should("::throw.class.simpleName= IllegalArgumentException");
                }
            }

            @Nested
            class SpecifyArg {
                private final String[] argv = {"--threads", "5", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_contains_threads_1_in_args() {
                    expect(master.masterArgs).should(": [... '--threads', '1' ...]");
                    expect(master.swarmArgs.getWorkerArgs()).should(": [... '--threads', '1' ...]");
                }

                @Test
                void force_threads_to_1() {
                    expect(buildOptions(master.masterArgs)).should("threads: 1");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should("threads: 1");
                }

                @Test
                void raise_error_when_get_remote_worker_args() {
                    expectRun(() -> master.swarmArgs.getRemoteWorkerArgs(1)).should("::throw.class.simpleName= IllegalArgumentException");
                }
            }
        }

        @Nested
        class WithRemoteWorker {

            @Nested
            class DefaultArg {
                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_contains_threads_1_in_args() {
                    expect(master.masterArgs).should(": [... '--threads', '1' ...]");
                    expect(master.swarmArgs.getWorkerArgs()).should(": [... '--threads', '1' ...]");
                }

                @Test
                void force_threads_to_1() {
                    expect(buildOptions(master.masterArgs)).should("threads: 1");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should("threads: 1");
                }

                @Nested
                class RemoteArgs {

                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
                    private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);

                    @Test
                    void should_contains_threads_1_in_remote_worker_args() {
                        expect(remoteArgs).should(": [... '--threads', '1' ...]");
                    }

                    @Test
                    void force_threads_to_1() {
                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should("threads: 1");
                    }
                }
            }

            @Nested
            class SpecifyArg {
                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "--threads", "5", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_contains_threads_1_in_args() {
                    expect(master.masterArgs).should(": [... '--threads', '1' ...]");
                    expect(master.swarmArgs.getWorkerArgs()).should(": [... '--threads', '1' ...]");
                }

                @Test
                void force_threads_to_1() {
                    expect(buildOptions(master.masterArgs)).should("threads: 1");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should("threads: 1");
                }

                @Nested
                class RemoteArgs {

                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
                    private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);

                    @Test
                    void should_contains_threads_1_in_remote_worker_args() {
                        expect(remoteArgs).should(": [... '--threads', '1' ...]");
                    }

                    @Test
                    void force_threads_to_1() {
                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should("threads: 1");
                    }
                }
            }
        }
    }


//        @Nested
//        class WorkerPluginsNoSummaryHasForwarding {
//
//            @Test
//            void has_no_summary_arg() {
//                String[] argv = {"--no-summary", "features"};
//                ProcessedArgs result = preProcessor.process(argv, classLoader);
//
//                expect(buildOptions(result.masterArgs)).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.MasterPlugin}]");
//                expect(buildOptions(result.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}]");
//            }
//
//            @Test
//            void no_no_summary_arg() {
//                String[] argv = {"features"};
//                ProcessedArgs result = preProcessor.process(argv, classLoader);
//
//                expect(buildOptions(result.masterArgs)).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.MasterPlugin}" +
//                        "{pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
//                expect(buildOptions(result.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}]");
//            }
//        }
//
//        @Nested
//        class DisableAllOtherPluginsOnLocalWorker {
//
//            @Test
//            void disable_all_other_plugins_on_local_worker() {
//                String[] argv = {"--plugin", "pretty", "features"};
//                ProcessedArgs result = preProcessor.process(argv, classLoader);
//
//                expect(buildOptions(result.masterArgs)).should("plugins: [" +
//                        "{pluginString: org.testcharm.cucumber.swarm.MasterPlugin} " +
//                        "{pluginString: pretty} " +
//                        "{pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter} " +
//                        "]");
//                expect(buildOptions(result.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}]");
//            }
//        }
//
//        @Nested
//        class WorkerTimeout {
//
//            @Test
//            void default_worker_timeout() {
//                String[] argv = {"features"};
//                ProcessedArgs result = preProcessor.process(argv, classLoader);
//
//                expect(result.swarmArgs).should("workerTimeout: 5");
//            }
//
//            @Test
//            void worker_timeout() {
//                String[] argv = {"--worker-timeout", "120", "features"};
//                ProcessedArgs result = preProcessor.process(argv, classLoader);
//
//                expect(result.swarmArgs).should("workerTimeout: 120");
//            }
//        }
//
//        @Nested
//        class WorkingPath {
//
//            @Test
//            void default_working_path_is_current_project_path() {
//                String[] argv = {"features"};
//                ProcessedArgs result = preProcessor.process(argv, classLoader);
//
//                expect(result.swarmArgs.getWorkingDir()).isEqualTo(System.getProperty("user.dir"));
//            }
//
//            @Test
//            void specify_working_path() {
//                String[] argv = {"--working-dir", "/tmp", "features"};
//                ProcessedArgs result = preProcessor.process(argv, classLoader);
//
//                expect(result.swarmArgs.getWorkingDir()).isEqualTo("/tmp");
//            }
//        }
//    }
//
//    @Nested
//    class WithRemoteWorker {
//
//        @Nested
//        class SwarmMode {
//

    /// /            @Test
    /// /            void disable_local_worker() {
    /// /                String[] argv = {"--local-worker", "disable", "features", "--", "ls"};
    /// /                ProcessedArgs result = preProcessor.process(argv, classLoader);
    /// /
    /// /                expect(result.swarmArgs).should("localWorker: false");
    /// /            }
//        }
//
//        @Nested
//        class RemoteWorkerCount {
//
//            @Test
//            void default_remote_worker_count() {
//                String[] argv = {"features", "--", "ls"};
//                ProcessedArgs result = preProcessor.process(argv, classLoader);
//
//                expect(result.swarmArgs).should("remoteWorkerCount: 0");
//            }
//
//            @Test
//            void specify_remote_worker_count() {
//                String[] argv = {"--remote-worker-count", "5", "features", "--", "ls"};
//                ProcessedArgs result = preProcessor.process(argv, classLoader);
//
//                expect(result.swarmArgs).should("remoteWorkerCount: 5");
//            }
//        }
//
//        @Nested
//        class RemoteWorkerArgs {
//
//            @Test
//            void no_remote_worker_args() {
//                String[] argv = {"features"};
//                ProcessedArgs result = preProcessor.process(argv, classLoader);
//
//                expect(result.swarmArgs.getRemoteWorkerArgs(43)).should("= []");
//            }
//
//            @Test
//            void remote_worker_args() {
//                String[] argv = {"features", "--", "ls", "{worker-id}"};
//                ProcessedArgs result = preProcessor.process(argv, classLoader);
//
//                expect(result.swarmArgs.getRemoteWorkerArgs(43)).should(": [ls, '43']");
//            }
//        }
//
//        @Nested
//        class RemoteSite {
//
//            @Test
//            void master() {
//                String[] argv = {"features"};
//                ProcessedArgs result = preProcessor.process(argv, classLoader);
//
//                expect(result.swarmArgs).should("workerId: null");
//            }
//
//            @Test
//            void worker() {
//                String[] argv = {"--worker-id", "100", "features"};
//                ProcessedArgs result = preProcessor.process(argv, classLoader);
//
//                expect(result.swarmArgs).should("workerId: 100");
//            }
//        }
//
//        @Nested
//        class NoSummary {
//
//            @Test
//            void no_no_summary_arg() {
//                String[] argv = {"--worker-id", "100", "features"};
//                ProcessedArgs result = preProcessor.process(argv, classLoader);
//
//                expect(buildOptions(result.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}" +
//                        "{pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
//            }
//        }
//    }
    private RuntimeOptions buildOptions(String[] argv) {
        return new CommandlineOptionsParser(System.out).parse(argv)
                .addDefaultSummaryPrinterIfNotDisabled()
                .enablePublishPlugin()
                .build(RuntimeOptions.defaultOptions());
    }
}