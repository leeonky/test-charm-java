package com.github.leeonky.summary.cucumber;

import org.junit.jupiter.api.Test;

import static io.cucumber.core.cli.Main.run;
import static org.assertj.core.api.Assertions.assertThat;

class Run {

    @Test
    void run_cucumber() {
        assertThat(run("--plugin", "pretty", "--glue", "com.github.leeonky", "--threads",
                String.valueOf(threadsCount("COMPILER_THREAD_SIZE", 8)),
                "src/test/resources/features/")).isEqualTo(Byte.valueOf("0"));
    }

    private static int threadsCount(String env, int defaultValue) {
        String value = System.getenv(env);
        if (value == null)
            return defaultValue;
        return Integer.parseInt(value);
    }
}
