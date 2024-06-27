package com.github.leeonky.cucumber;

import org.junit.jupiter.api.Test;

import static io.cucumber.core.cli.Main.run;
import static org.assertj.core.api.Assertions.assertThat;

public class RunCucumberModuleSafe {
    @Test
    void run_cucumber() {
        assertThat(run("--plugin", "pretty", "--glue", "com.github.leeonky",
                "src/test/resources/features/module-safe")).isEqualTo(Byte.valueOf("0"));
    }
}
