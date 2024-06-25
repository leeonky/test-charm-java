package com.github.leeonky.cucumber.restful;

import com.github.leeonky.cucumber.restful.extensions.PathVariableReplacement;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class EvaluatorTest {

    Evaluator evaluator = new Evaluator();

    @Test
    public void not_replace() {
        PathVariableReplacement.reset();

        String value = evaluator.eval("${NotExist}");

        Assertions.assertThat(value).isEqualTo("${NotExist}");
    }

    @Test
    public void replace_with_var_value() {
        PathVariableReplacement.evaluator = var -> {
            Assertions.assertThat(var).isEqualTo("var");
            return "value";
        };

        String value = evaluator.eval("${var}");

        Assertions.assertThat(value).isEqualTo("value");
    }

}