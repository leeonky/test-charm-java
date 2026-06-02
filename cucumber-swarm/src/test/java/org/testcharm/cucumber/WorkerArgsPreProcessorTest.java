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
                void auto_append_option_worker_id() {
                    expect(remoteArgs).should(": [... '--worker-id' '43' ...]");
                }

                @Test
                void build_option_with_no_error() {
                    expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should(": {...}");
                }
            }
        }

        @Nested
        class RemoteWorkerFeaturePath {

            @Nested
            class RelativePath {

                @Test
                void auto_append_feature_dir() {
                    String[] remoteArgs = preProcessor.process(new String[]{"--remote-worker-launcher", "run-cucumber.sh", "features"},
                            classLoader).swarmArgs.getRemoteWorkerArgs(43);

                    expect(remoteArgs).should(": [... 'features']");
                }

                @Test
                void auto_append_feature_file() {
                    String[] remoteArgs = preProcessor.process(new String[]{"--remote-worker-launcher", "run-cucumber.sh", "features/a.feature"},
                            classLoader).swarmArgs.getRemoteWorkerArgs(43);

                    expect(remoteArgs).should(": [... 'features/a.feature']");
                }

                @Test
                void multiple_targets() {
                    String[] remoteArgs = preProcessor.process(new String[]{"--remote-worker-launcher", "run-cucumber.sh", "features/a.feature", "features/b.feature"},
                            classLoader).swarmArgs.getRemoteWorkerArgs(43);

                    expect(remoteArgs).should(": [... 'features/a.feature' 'features/b.feature']");
                }

                @Test
                void file_with_line() {
                    String[] remoteArgs = preProcessor.process(new String[]{"--remote-worker-launcher", "run-cucumber.sh", "features/a.feature:10"},
                            classLoader).swarmArgs.getRemoteWorkerArgs(43);

                    expect(remoteArgs).should(": [... 'features/a.feature:10']");
                }
            }

            @Nested
            class AbsolutePath {

                @Test
                void auto_append_feature_dir() {
                    String[] remoteArgs = preProcessor.process(new String[]{"--remote-worker-launcher", "run-cucumber.sh",
                            System.getProperty("user.dir") + "/features"}, classLoader).swarmArgs.getRemoteWorkerArgs(43);

                    expect(remoteArgs).should(": [... features]");
                }

                @Test
                void auto_append_feature_file() {
                    String[] remoteArgs = preProcessor.process(new String[]{"--remote-worker-launcher", "run-cucumber.sh",
                            System.getProperty("user.dir") + "/features/a.feature"}, classLoader).swarmArgs.getRemoteWorkerArgs(43);

                    expect(remoteArgs).should(": [... features/a.feature]");
                }

                @Test
                void multiple_targets() {
                    String[] remoteArgs = preProcessor.process(new String[]{"--remote-worker-launcher", "run-cucumber.sh",
                                    System.getProperty("user.dir") + "/features/a.feature", System.getProperty("user.dir") + "/features/b.feature"},
                            classLoader).swarmArgs.getRemoteWorkerArgs(43);

                    expect(remoteArgs).should(": [... features/a.feature features/b.feature]");
                }

                @Test
                void file_with_line() {
                    String[] remoteArgs = preProcessor.process(new String[]{"--remote-worker-launcher", "run-cucumber.sh",
                                    System.getProperty("user.dir") + "/features/a.feature:10"},
                            classLoader).swarmArgs.getRemoteWorkerArgs(43);

                    expect(remoteArgs).should(": [... features/a.feature:10]");
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
                    expect(master.swarmArgs).should("swarmHost.host= 'localhost'");
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

    @Nested
    class OptionWorkerTimeout {

        @Nested
        class WithoutRemoteWorker {

            @Nested
            class DefaultArg {
                private final String[] argv = {"features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--worker-timeout'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--worker-timeout'");
                }

                @Test
                void default_worker_timeout() {
                    expect(master.swarmArgs).should("workerTimeout= 40");
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
                private final String[] argv = {"--worker-timeout", "10", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--worker-timeout'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--worker-timeout'");
                }

                @Test
                void default_worker_timeout() {
                    expect(master.swarmArgs).should("workerTimeout= 10");
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
                    expect(master.masterArgs).should("::should::not.contains: '--worker-timeout'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--worker-timeout'");
                }

                @Test
                void default_worker_timeout() {
                    expect(master.swarmArgs).should("workerTimeout= 40");
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
                        expect(remoteArgs).should("::should::not.contains: '--worker-timeout'");
                    }

                    @Test
                    void build_option_with_no_error() {
                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should(": {...}");
                    }

                    @Test
                    void default_swarm_part() {
                        // not use in remote worker
                        expect(remote.swarmArgs).should("workerTimeout= 40");
                    }
                }
            }

            @Nested
            class SpecifyArg {
                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "--worker-timeout", "10", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--worker-timeout'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--worker-timeout'");
                }

                @Test
                void default_worker_timeout() {
                    expect(master.swarmArgs).should("workerTimeout= 10");
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
                        expect(remoteArgs).should("::should::not.contains: '--worker-timeout'");
                    }

                    @Test
                    void build_option_with_no_error() {
                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should(": {...}");
                    }

                    @Test
                    void default_swarm_part() {
                        // not use in remote worker
                        expect(remote.swarmArgs).should("workerTimeout= 40");
                    }
                }
            }
        }
    }

    @Nested
    class OptionRemoteWorkerCount {

        @Nested
        class WithoutRemoteWorker {

            @Nested
            class DefaultArg {
                private final String[] argv = {"features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--remote-worker-count'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--remote-worker-count'");
                }

                @Test
                void default_remote_worker_count() {
                    expect(master.swarmArgs).should("remoteWorkerCount= 0");
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
                private final String[] argv = {"--remote-worker-count", "10", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--remote-worker-count'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--remote-worker-count'");
                }

                @Test
                void default_remote_worker_count() {
                    expect(master.swarmArgs).should("remoteWorkerCount= 10");
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
                    expect(master.masterArgs).should("::should::not.contains: '--remote-worker-count'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--remote-worker-count'");
                }

                @Test
                void default_remote_worker_count() {
                    expect(master.swarmArgs).should("remoteWorkerCount= 0");
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
                        expect(remoteArgs).should("::should::not.contains: '--remote-worker-count'");
                    }

                    @Test
                    void build_option_with_no_error() {
                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should(": {...}");
                    }

                    @Test
                    void default_swarm_part() {
                        // not use in remote worker
                        expect(remote.swarmArgs).should("remoteWorkerCount= 0");
                    }
                }
            }

            @Nested
            class SpecifyArg {
                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "--worker-timeout", "10", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--worker-timeout'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--worker-timeout'");
                }

                @Test
                void default_worker_timeout() {
                    expect(master.swarmArgs).should("workerTimeout= 10");
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
                        expect(remoteArgs).should("::should::not.contains: '--remote-worker-count'");
                    }

                    @Test
                    void build_option_with_no_error() {
                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should(": {...}");
                    }

                    @Test
                    void default_swarm_part() {
                        // not use in remote worker
                        expect(remote.swarmArgs).should("remoteWorkerCount= 0");
                    }
                }
            }
        }
    }

    @Nested
    class OptionPlugin {

        @Nested
        class WithoutRemoteWorker {

            @Nested
            class DefaultArg {
                private final String[] argv = {"features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void master_should_contains_master_plugin_and_default_summary_printer() {
                    expect(master.masterArgs).should(": [... '--plugin' 'org.testcharm.cucumber.swarm.MasterPlugin' ...]");
                    expect(buildOptions(master.masterArgs)).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.MasterPlugin}" +
                            "{pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
                }

                @Test
                void local_worker_should_only_contains_worker_forwarding_plugin_and_no_default_summary() {
                    expect(master.swarmArgs.getWorkerArgs()).should(": [... '--no-summary' '--plugin' 'org.testcharm.cucumber.swarm.WorkerForwardingPlugin' ...]");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}]");
                }

                @Test
                void raise_error_when_get_remote_worker_args() {
                    expectRun(() -> master.swarmArgs.getRemoteWorkerArgs(1)).should("::throw.class.simpleName= IllegalArgumentException");
                }
            }

            @Nested
            class NoSummary {

                private final String[] argv = {"--no-summary", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void master_should_contains_master_plugin_and_default_summary_printer() {
                    expect(master.masterArgs).should(": [... '--plugin' 'org.testcharm.cucumber.swarm.MasterPlugin' '--no-summary' ...]");
                    expect(buildOptions(master.masterArgs)).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.MasterPlugin}]");
                }

                @Test
                void local_worker_should_only_contains_worker_forwarding_plugin_and_no_default_summary() {
                    expect(master.swarmArgs.getWorkerArgs()).should(": [... '--no-summary' '--plugin' 'org.testcharm.cucumber.swarm.WorkerForwardingPlugin' ...]");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}]");
                }

                @Test
                void raise_error_when_get_remote_worker_args() {
                    expectRun(() -> master.swarmArgs.getRemoteWorkerArgs(1)).should("::throw.class.simpleName= IllegalArgumentException");
                }
            }

            @Nested
            class OtherPlugin {

                private final String[] argv = {"--plugin", "pretty", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void master_should_contains_master_plugin_and_default_summary_printer() {
                    expect(master.masterArgs).should(": [... '--plugin' 'org.testcharm.cucumber.swarm.MasterPlugin' '--plugin' pretty ...]");
                    expect(buildOptions(master.masterArgs)).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.MasterPlugin}" +
                            "{pluginString: pretty} {pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
                }

                @Test
                void local_worker_should_only_contains_worker_forwarding_plugin_and_no_default_summary() {
                    expect(master.swarmArgs.getWorkerArgs()).should(": [... '--no-summary' '--plugin' 'org.testcharm.cucumber.swarm.WorkerForwardingPlugin' ...]");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: pretty");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}]");
                }

                @Test
                void raise_error_when_get_remote_worker_args() {
                    expectRun(() -> master.swarmArgs.getRemoteWorkerArgs(1)).should("::throw.class.simpleName= IllegalArgumentException");
                }
            }

            @Nested
            class NoSummaryWithOtherPlugin {

                private final String[] argv = {"--no-summary", "--plugin", "pretty", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void master_should_contains_master_plugin_and_default_summary_printer() {
                    expect(master.masterArgs).should(": [... '--plugin' 'org.testcharm.cucumber.swarm.MasterPlugin' '--no-summary' '--plugin' pretty ...]");
                    expect(buildOptions(master.masterArgs)).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.MasterPlugin}" +
                            "{pluginString: pretty}]");
                }

                @Test
                void local_worker_should_only_contains_worker_forwarding_plugin_and_no_default_summary() {
                    expect(master.swarmArgs.getWorkerArgs()).should(": [... '--no-summary' '--plugin' 'org.testcharm.cucumber.swarm.WorkerForwardingPlugin' ...]");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: pretty");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}]");
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
                void master_should_contains_master_plugin_and_default_summary_printer() {
                    expect(master.masterArgs).should(": [... '--plugin' 'org.testcharm.cucumber.swarm.MasterPlugin' ...]");
                    expect(buildOptions(master.masterArgs)).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.MasterPlugin}" +
                            "{pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
                }

                @Test
                void local_worker_should_only_contains_worker_forwarding_plugin_and_no_default_summary() {
                    expect(master.swarmArgs.getWorkerArgs()).should(": [... '--no-summary' '--plugin' 'org.testcharm.cucumber.swarm.WorkerForwardingPlugin' ...]");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}]");
                }

                @Nested
                class RemoteArgs {
                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
                    private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);

                    @Test
                    void remote_worker_should_contains_worker_forwarding_plugin() {
                        expect(remoteArgs).should("::should::not.contains: '--no-summary'");
                        expect(remoteArgs).should("::should::not.contains: '--plugin'");

                        expect(remote.swarmArgs.getWorkerArgs()).should(": [... '--plugin' 'org.testcharm.cucumber.swarm.WorkerForwardingPlugin' ...]");
                        expect(remote.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--no-summary'");

                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should("plugins: " +
                                "[{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}" +
                                "{pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
                    }
                }
            }

            @Nested
            class NoSummary {

                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "--no-summary", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void master_should_contains_master_plugin_and_default_summary_printer() {
                    expect(master.masterArgs).should(": [... '--plugin' 'org.testcharm.cucumber.swarm.MasterPlugin' '--no-summary' ...]");
                    expect(buildOptions(master.masterArgs)).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.MasterPlugin}]");
                }

                @Test
                void local_worker_should_only_contains_worker_forwarding_plugin_and_no_default_summary() {
                    expect(master.swarmArgs.getWorkerArgs()).should(": [... '--no-summary' '--plugin' 'org.testcharm.cucumber.swarm.WorkerForwardingPlugin' ...]");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}]");
                }

                @Nested
                class RemoteArgs {
                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
                    private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);

                    @Test
                    void remote_worker_should_contains_worker_forwarding_plugin() {
                        expect(remoteArgs).should("::should::not.contains: '--no-summary'");
                        expect(remoteArgs).should("::should::not.contains: '--plugin'");

                        expect(remote.swarmArgs.getWorkerArgs()).should(": [... '--plugin' 'org.testcharm.cucumber.swarm.WorkerForwardingPlugin' ...]");
                        expect(remote.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--no-summary'");

                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should("plugins: " +
                                "[{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}" +
                                "{pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
                    }
                }
            }

            @Nested
            class OtherPlugin {

                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "--plugin", "pretty", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void master_should_contains_master_plugin_and_default_summary_printer() {
                    expect(master.masterArgs).should(": [... '--plugin' 'org.testcharm.cucumber.swarm.MasterPlugin' '--plugin' pretty ...]");
                    expect(buildOptions(master.masterArgs)).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.MasterPlugin}" +
                            "{pluginString: pretty} {pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
                }

                @Test
                void local_worker_should_only_contains_worker_forwarding_plugin_and_no_default_summary() {
                    expect(master.swarmArgs.getWorkerArgs()).should(": [... '--no-summary' '--plugin' 'org.testcharm.cucumber.swarm.WorkerForwardingPlugin' ...]");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}]");
                }

                @Nested
                class RemoteArgs {
                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
                    private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);

                    @Test
                    void remote_worker_should_contains_worker_forwarding_plugin() {
                        expect(remoteArgs).should("::should::not.contains: '--no-summary'");
                        expect(remoteArgs).should("::should::not.contains: '--plugin'");

                        expect(remote.swarmArgs.getWorkerArgs()).should(": [... '--plugin' 'org.testcharm.cucumber.swarm.WorkerForwardingPlugin' ...]");
                        expect(remote.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--no-summary'");
                        expect(remote.swarmArgs.getWorkerArgs()).should("::should::not.contains: pretty");

                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should("plugins: " +
                                "[{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}" +
                                "{pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
                    }
                }
            }

            @Nested
            class NoSummaryWithOtherPlugin {

                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "--no-summary", "--plugin", "pretty", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void master_should_contains_master_plugin_and_default_summary_printer() {
                    expect(master.masterArgs).should(": [... '--plugin' 'org.testcharm.cucumber.swarm.MasterPlugin' '--no-summary' '--plugin' pretty ...]");
                    expect(buildOptions(master.masterArgs)).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.MasterPlugin}" +
                            "{pluginString: pretty}]");
                }

                @Test
                void local_worker_should_only_contains_worker_forwarding_plugin_and_no_default_summary() {
                    expect(master.swarmArgs.getWorkerArgs()).should(": [... '--no-summary' '--plugin' 'org.testcharm.cucumber.swarm.WorkerForwardingPlugin' ...]");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should("plugins: [{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}]");
                }

                @Nested
                class RemoteArgs {
                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
                    private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);

                    @Test
                    void remote_worker_should_contains_worker_forwarding_plugin() {
                        expect(remoteArgs).should("::should::not.contains: '--no-summary'");
                        expect(remoteArgs).should("::should::not.contains: '--plugin'");

                        expect(remote.swarmArgs.getWorkerArgs()).should(": [... '--plugin' 'org.testcharm.cucumber.swarm.WorkerForwardingPlugin' ...]");
                        expect(remote.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--no-summary'");
                        expect(remote.swarmArgs.getWorkerArgs()).should("::should::not.contains: pretty");

                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should("plugins: " +
                                "[{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}" +
                                "{pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
                    }
                }
            }

            @Nested
            class RemoteWorkerPlugin {
                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "--remote-options-json", "[\"--plugin\",\"pretty\"]", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
                private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);

                @Test
                void remote_worker_should_contains_worker_forwarding_plugin_and_pretty() {
                    expect(remoteArgs).should("::should::not.contains: '--no-summary'");
                    expect(remoteArgs).should(": [... '--plugin' pretty ...]");

                    expect(remote.swarmArgs.getWorkerArgs()).should(": [... '--plugin' pretty '--plugin' 'org.testcharm.cucumber.swarm.WorkerForwardingPlugin' ...]");
                    expect(remote.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--no-summary'");

                    expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should("plugins: " +
                            "[{pluginString: pretty}" +
                            "{pluginString: org.testcharm.cucumber.swarm.WorkerForwardingPlugin}" +
                            "{pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
                }
            }
        }
    }

    @Nested
    class OptionRemoteOptionsJson {

        @Nested
        class WithoutRemoteWorker {

            @Nested
            class NoArg {
                private final String[] argv = {"features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--remote-options-json'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--remote-options-json'");
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
            class SetArg {
                private final String[] argv = {"--remote-options-json", "[]", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--remote-options-json'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--remote-options-json'");
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
            class NoArg {
                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--remote-options-json'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--remote-options-json'");
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
                    void remote_worker_should_contains_worker_forwarding_plugin() {
                        expect(remoteArgs).should("::should::not.contains: '--remote-options-json'");
                        expect(remote.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--remote-options-json'");
                    }
                }
            }

            @Nested
            class SetArg {
                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "--remote-options-json", "[\"--key\", 100]", "features"};
                private final ProcessedArgs master = preProcessor.process(argv, classLoader);

                @Test
                void should_not_contains_in_args() {
                    expect(master.masterArgs).should("::should::not.contains: '--remote-options-json'");
                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--remote-options-json'");
                }

                @Test
                void build_option_with_no_error() {
                    expect(buildOptions(master.masterArgs)).should(": {...}");
                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
                }

                @Nested
                class RemoteArgs {
                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);

                    @Test
                    void remote_worker_should_contains_worker_forwarding_plugin() {
                        expect(remoteArgs).should(": [... '--key' '100' ...]");
                    }

                    @Test
                    void should_after_remote_worker_launcher() {
                        expect(remoteArgs).should(": ['run-cucumber.sh' '--key' '100' ...]");
                    }
                }
            }
        }
    }

    //    @Nested
//    class OptionWorkingPath {
//
//        @Nested
//        class WithoutRemoteWorker {
//
//            @Nested
//            class DefaultArg {
//                private final String[] argv = {"features"};
//                private final ProcessedArgs master = preProcessor.process(argv, classLoader);
//
//                @Test
//                void should_not_contains_in_args() {
//                    expect(master.masterArgs).should("::should::not.contains: '--working-dir'");
//                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--working-dir'");
//                }
//
//                @Test
//                void default_working_dir() {
//                    expect(master.swarmArgs.getWorkingDir()).isEqualTo(System.getProperty("user.dir"));
//                }
//
//                @Test
//                void build_option_with_no_error() {
//                    expect(buildOptions(master.masterArgs)).should(": {...}");
//                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
//                }
//
//                @Test
//                void raise_error_when_get_remote_worker_args() {
//                    expectRun(() -> master.swarmArgs.getRemoteWorkerArgs(1)).should("::throw.class.simpleName= IllegalArgumentException");
//                }
//            }
//
//            @Nested
//            class SpecifyArg {
//                private final String[] argv = {"--working-dir", "/tmp", "features"};
//                private final ProcessedArgs master = preProcessor.process(argv, classLoader);
//
//                @Test
//                void should_not_contains_in_args() {
//                    expect(master.masterArgs).should("::should::not.contains: '--working-dir'");
//                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--working-dir'");
//                }
//
//                @Test
//                void working_dir() {
//                    expect(master.swarmArgs.getWorkingDir()).isEqualTo("/tmp");
//                }
//
//                @Test
//                void build_option_with_no_error() {
//                    expect(buildOptions(master.masterArgs)).should(": {...}");
//                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
//                }
//
//                @Test
//                void raise_error_when_get_remote_worker_args() {
//                    expectRun(() -> master.swarmArgs.getRemoteWorkerArgs(1)).should("::throw.class.simpleName= IllegalArgumentException");
//                }
//            }
//        }
//
//        @Nested
//        class WithRemoteWorker {
//
//            @Nested
//            class DefaultArg {
//                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "features"};
//                private final ProcessedArgs master = preProcessor.process(argv, classLoader);
//
//                @Test
//                void should_not_contains_in_args() {
//                    expect(master.masterArgs).should("::should::not.contains: '--working-dir'");
//                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--working-dir'");
//                }
//
//                @Test
//                void default_working_dir() {
//                    expect(master.swarmArgs.getWorkingDir()).isEqualTo(System.getProperty("user.dir"));
//                }
//
//                @Test
//                void build_option_with_no_error() {
//                    expect(buildOptions(master.masterArgs)).should(": {...}");
//                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
//                }
//
//                @Nested
//                class RemoteArgs {
//                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
//                    private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);
//
//                    @Test
//                    void remote_worker_should_contains_worker_forwarding_plugin() {
//                        expect(remoteArgs).should("::should::not.contains: '--working-dir'");
//                    }
//
//                    @Test
//                    void build_option_with_no_error() {
//                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should(": {...}");
//                    }
//
//                    @Test
//                    void default_working_dir() {
//                        expect(remote.swarmArgs.getWorkingDir()).isEqualTo(System.getProperty("user.dir"));
//                    }
//                }
//            }
//
//            @Nested
//            class SpecifyArg {
//                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "--working-dir", "/tmp", "features"};
//                private final ProcessedArgs master = preProcessor.process(argv, classLoader);
//
//                @Test
//                void should_not_contains_in_args() {
//                    expect(master.masterArgs).should("::should::not.contains: '--working-dir'");
//                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--working-dir'");
//                }
//
//                @Test
//                void default_swarm_host() {
//                    expect(master.swarmArgs.getWorkingDir()).isEqualTo("/tmp");
//                }
//
//                @Test
//                void build_option_with_no_error() {
//                    expect(buildOptions(master.masterArgs)).should(": {...}");
//                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
//                }
//
//                @Nested
//                class RemoteArgs {
//                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
//                    private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);
//
//                    @Test
//                    void remote_worker_should_contains_worker_forwarding_plugin() {
//                        expect(remoteArgs).should("::should::not.contains: '--working-dir'");
//                    }
//
//                    @Test
//                    void build_option_with_no_error() {
//                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should(": {...}");
//                    }
//
//                    @Test
//                    void default_working_dir() {
//                        expect(remote.swarmArgs.getWorkingDir()).isEqualTo(System.getProperty("user.dir"));
//                    }
//                }
//            }
//        }
//    }
//
//    @Nested
//    class OptionRemoteWorkingPath {
//
//        @Nested
//        class WithoutRemoteWorker {
//
//            @Nested
//            class SpecifyArg {
//                private final String[] argv = {"--remote-working-dir", "/tmp", "features"};
//                private final ProcessedArgs master = preProcessor.process(argv, classLoader);
//
//                @Test
//                void should_not_contains_in_args() {
//                    expect(master.masterArgs).should("::should::not.contains: '--remote-working-dir'");
//                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--remote-working-dir'");
//                }
//
//                @Test
//                void build_option_with_no_error() {
//                    expect(buildOptions(master.masterArgs)).should(": {...}");
//                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
//                }
//
//                @Test
//                void raise_error_when_get_remote_worker_args() {
//                    expectRun(() -> master.swarmArgs.getRemoteWorkerArgs(1)).should("::throw.class.simpleName= IllegalArgumentException");
//                }
//            }
//        }
//
//        @Nested
//        class WithRemoteWorker {
//
//            @Nested
//            class DefaultArg {
//                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "features"};
//                private final ProcessedArgs master = preProcessor.process(argv, classLoader);
//
//                @Test
//                void should_not_contains_in_args() {
//                    expect(master.masterArgs).should("::should::not.contains: '--remote--working-dir'");
//                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--remote--working-dir'");
//                }
//
//                @Test
//                void build_option_with_no_error() {
//                    expect(buildOptions(master.masterArgs)).should(": {...}");
//                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
//                }
//
//                @Nested
//                class RemoteArgs {
//                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
//                    private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);
//
//                    @Test
//                    void should_not_contains_in_args() {
//                        expect(remoteArgs).should("::should::not.contains: '--working-dir'");
//                        expect(remoteArgs).should("::should::not.contains: '--remote--working-dir'");
//                    }
//
//                    @Test
//                    void build_option_with_no_error() {
//                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should(": {...}");
//                    }
//
//                    @Test
//                    void default_working_dir() {
//                        expect(remote.swarmArgs.getWorkingDir()).isEqualTo(System.getProperty("user.dir"));
//                    }
//                }
//            }
//
//            @Nested
//            class SpecifyArg {
//                private final String[] argv = {"--remote-worker-launcher", "run-cucumber.sh", "--remote-working-dir", "/tmp", "features"};
//                private final ProcessedArgs master = preProcessor.process(argv, classLoader);
//
//                @Test
//                void should_not_contains_in_args() {
//                    expect(master.masterArgs).should("::should::not.contains: '--remote-working-dir'");
//                    expect(master.swarmArgs.getWorkerArgs()).should("::should::not.contains: '--remote-working-dir'");
//                }
//
//                @Test
//                void build_option_with_no_error() {
//                    expect(buildOptions(master.masterArgs)).should(": {...}");
//                    expect(buildOptions(master.swarmArgs.getWorkerArgs())).should(": {...}");
//                }
//
//                @Nested
//                class RemoteArgs {
//                    private final String[] remoteArgs = master.swarmArgs.getRemoteWorkerArgs(43);
//                    private final ProcessedArgs remote = preProcessor.process(remoteArgs, classLoader);
//
//                    @Test
//                    void should_change_option_to_remote_working_dir() {
//                        expect(remoteArgs).should("::should::not.contains: '--remote--working-dir'");
//                        expect(remoteArgs).should(": [... '--working-dir' '/tmp' ...]");
//                    }
//
//                    @Test
//                    void build_option_with_no_error() {
//                        expect(buildOptions(remote.swarmArgs.getWorkerArgs())).should(": {...}");
//                    }
//
//                    @Test
//                    void change_working_dir() {
//                        expect(remote.swarmArgs.getWorkingDir()).isEqualTo("/tmp");
//                    }
//                }
//            }
//        }
//    }
//
    private RuntimeOptions buildOptions(String[] argv) {
        return new CommandlineOptionsParser(System.out).parse(argv)
                .addDefaultSummaryPrinterIfNotDisabled()
                .enablePublishPlugin()
                .build(RuntimeOptions.defaultOptions());
    }
}