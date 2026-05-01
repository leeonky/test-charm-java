package org.testcharm.cucumber;

import org.junit.jupiter.api.Test;

import static io.cucumber.core.cli.Main.run;
import static org.assertj.core.api.Assertions.assertThat;

public class Run {

    @Test
    void run_cucumber() {
        assertThat(run("--plugin", "pretty", "--glue", "org.testcharm",
                "--threads", "8", "src/test/resources/features")).isEqualTo(Byte.valueOf("0"));
    }
}
