package com.github.leeonky.dal.extensions.inspector.cucumber;

import org.junit.jupiter.api.Test;

import static com.github.leeonky.dal.Assertions.expect;
import static io.cucumber.core.cli.Main.run;

public class RunCucumber {

    @Test
    void run_cucumber() {
        expect(run("--plugin", "pretty", "--glue", "com.github.leeonky", "src/test/resources/features")).should(": 0");
    }
}
