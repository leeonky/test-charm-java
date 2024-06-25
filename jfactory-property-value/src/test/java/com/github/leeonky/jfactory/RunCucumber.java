package com.github.leeonky.jfactory;

import org.junit.jupiter.api.Test;

import static io.cucumber.core.cli.Main.run;
import static org.assertj.core.api.Assertions.assertThat;

public class RunCucumber {

    @Test
    void run_cucumber() {
        assertThat(run("--plugin", "pretty", "--glue", "com.github.leeonky",
                "src/test/resources/features")).isEqualTo(Byte.valueOf("0"));
    }
}
