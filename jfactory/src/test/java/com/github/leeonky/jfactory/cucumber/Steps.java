package com.github.leeonky.jfactory.cucumber;

import com.github.leeonky.util.JavaExecutor;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;

public class Steps {

    @Before
    public void importDependencies() {
        JavaExecutor.executor().main().importDependency("com.github.leeonky.jfactory.*");
    }

    @Given("the following spec definition:")
    public void theFollowingSpecDefinition(String sourceCode) {
        JavaExecutor.executor().addClass(
                "import com.github.leeonky.jfactory.Spec;\n" +
                        "import com.github.leeonky.jfactory.Global;\n" +
                        "import com.github.leeonky.jfactory.Instance;\n" +
                        "import com.github.leeonky.jfactory.Trait;\n" + sourceCode);
    }
}
