package com.github.leeonky.jfactory.cucumber;

import org.junit.jupiter.api.Test;

import static com.github.leeonky.jfactory.cucumber.IntegrationTestContextLegacy.threadsCount;
import static io.cucumber.core.cli.Main.run;
import static org.assertj.core.api.Assertions.assertThat;

class Run {

    @Test
    void run_cucumber() {
        assertThat(run("--plugin", "pretty", "--glue", "com.github.leeonky", "--threads",
                String.valueOf(threadsCount("COMPILER_THREAD_SIZE", 8)),
                "src/test/resources/features/0_core/")).isEqualTo(Byte.valueOf("0"));
    }
}
