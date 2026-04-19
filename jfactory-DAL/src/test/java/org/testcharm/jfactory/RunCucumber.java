package org.testcharm.jfactory;


import org.junit.jupiter.api.Test;

import static io.cucumber.core.cli.Main.run;
import static org.testcharm.dal.Assertions.expect;

class RunCucumber {

    @Test
    void run_cucumber() {
        expect(run("--plugin", "pretty", "--glue", "org.testcharm", "src/test/resources/features")).should(": 0");
    }
}
