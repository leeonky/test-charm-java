package org.testcharm.cucumber;

import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.options.RuntimeOptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.util.Pair;

import static org.testcharm.dal.Assertions.expect;

class WorkerArgsPreProcessorTest {
    WorkerArgsPreProcessor preProcessor = new WorkerArgsPreProcessor();

    @Nested
    class ForceSingleThread {

        @Test
        void has_thread_arg() {
            String[] argv = {"--threads", "4", "features"};
            Pair<String[], String[]> result = preProcessor.process(argv);

            expect(buildOptions(result.getFirst())).should("threads: 1");
            expect(buildOptions(result.getSecond())).should("threads: 1");
        }

        @Test
        void no_thread_arg() {
            String[] argv = {"features"};
            Pair<String[], String[]> result = preProcessor.process(argv);

            expect(buildOptions(result.getFirst())).should("threads: 1");
            expect(buildOptions(result.getSecond())).should("threads: 1");
        }
    }

    @Nested
    class WorkerPluginsNoSummaryHasForwarding {

        @Test
        void has_no_summary_arg() {
            String[] argv = {"--no-summary", "features"};
            Pair<String[], String[]> result = preProcessor.process(argv);

            expect(buildOptions(result.getFirst())).should("plugins: []");
            expect(buildOptions(result.getSecond())).should("plugins: [{pluginString: org.testcharm.cucumber.WorkerForwardingPlugin}]");
        }

        @Test
        void no_no_summary_arg() {
            String[] argv = {"features"};
            Pair<String[], String[]> result = preProcessor.process(argv);

            expect(buildOptions(result.getFirst())).should("plugins: [{pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
            expect(buildOptions(result.getSecond())).should("plugins: [{pluginString: org.testcharm.cucumber.WorkerForwardingPlugin}]");
        }
    }

    @Nested
    class DisableAllOtherPluginsOnLocalWorker {

        @Test
        void disable_all_other_plugins_on_local_worker() {
            String[] argv = {"--plugin", "pretty", "features"};
            Pair<String[], String[]> result = preProcessor.process(argv);

            expect(buildOptions(result.getFirst())).should("plugins: [{pluginString: pretty} {pluginString: io.cucumber.core.plugin.DefaultSummaryPrinter}]");
            expect(buildOptions(result.getSecond())).should("plugins: [{pluginString: org.testcharm.cucumber.WorkerForwardingPlugin}]");
        }
    }

    private RuntimeOptions buildOptions(String[] argv) {
        return new CommandlineOptionsParser(System.out).parse(argv)
                .addDefaultSummaryPrinterIfNotDisabled()
                .enablePublishPlugin()
                .build(RuntimeOptions.defaultOptions());
    }
}