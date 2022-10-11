package com.github.leeonky.dal.extensions;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static io.cucumber.core.cli.Main.run;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RunCucumber {

    @Test
    void run_cucumber() {
        List<String> args = new ArrayList<>(asList("--plugin", "pretty"));

        String value = System.getenv("CI");
        if (StringUtils.isBlank(value)) {
            args.add("--tags");
            args.add("not @ci-skip");
        }

        args.add("--glue");
        args.add("com.github.leeonky");

        args.add("src/test/resources/features");
        assertThat(run(args.toArray(new String[0]))).isEqualTo(Byte.valueOf("0"));
    }
}
