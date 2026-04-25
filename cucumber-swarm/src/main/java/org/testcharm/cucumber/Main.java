package org.testcharm.cucumber;

import static io.cucumber.core.cli.Main.run;

public class Main {

    public static void main(String... argv) {
        System.exit(run(argv, Thread.currentThread().getContextClassLoader()));
    }
}
