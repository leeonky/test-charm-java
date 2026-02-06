package com.github.leeonky.jfactory.collector;

import com.github.leeonky.util.JavaExecutor;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class Steps {

    @Before
    public void importDependencies() {
        JavaExecutor.executor().main().importDependency("com.github.leeonky.jfactory.*");
        JavaExecutor.executor().main().importDependency("com.github.leeonky.jfactory.collector.*");
        JavaExecutor.executor().main().importDependency("com.github.leeonky.dal.*");
    }

    @Given("the following spec definition:")
    public void theFollowingSpecDefinition(String sourceCode) {
        JavaExecutor.executor().addClass(
                "import com.github.leeonky.jfactory.Spec;\n" +
                        "import com.github.leeonky.jfactory.Global;\n" +
                        "import com.github.leeonky.jfactory.Instance;\n" +
                        "import com.github.leeonky.jfactory.Trait;\n" + sourceCode);
    }

    @When("{string} collect and build with the following properties:")
    public void collectAndBuildWithTheFollowingProperties(String collectorVarName,
                                                          String expressionForBuild) {
        String expressionForBuildVarName = "exp";
        JavaExecutor.executor().main().addArg(expressionForBuildVarName, expressionForBuild);
        JavaExecutor.executor().main().addRegisters(String.format("Assertions.expect(%s).should((String)args.get(\"%s\"))", collectorVarName, expressionForBuildVarName));
        JavaExecutor.executor().main().returnExpression(collectorVarName + ".build()");
    }
}
