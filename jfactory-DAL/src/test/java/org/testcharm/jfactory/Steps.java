package org.testcharm.jfactory;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.testcharm.util.JavaExecutor;

public class Steps {

    @Before
    public void importDependencies() {
        JavaExecutor.executor().main().importDependency("org.testcharm.jfactory.*");
        JavaExecutor.executor().main().importDependency("org.testcharm.util.*");
        JavaExecutor.executor().main().importDependency("org.testcharm.dal.*");
    }

    @Given("the following spec definition:")
    public void theFollowingSpecDefinition(String sourceCode) {
        JavaExecutor.executor().addClass(
                "import org.testcharm.jfactory.Spec;\n" +
                        "import org.testcharm.jfactory.Global;\n" +
                        "import org.testcharm.jfactory.Instance;\n" +
                        "import org.testcharm.jfactory.Trait;\n" + sourceCode);
    }

    @When("{string} collect and build with the following properties:")
    public void collectAndBuildWithTheFollowingProperties(String collectorVarName,
                                                          String expressionForBuild) {
        String expressionForBuildVarName = "exp";
        JavaExecutor.executor().main().addArg(expressionForBuildVarName, expressionForBuild);
        JavaExecutor.executor().main().addRegisters(String.format("Accessors.get((String)args.get(\"%s\")).from(%s)", expressionForBuildVarName, collectorVarName));
        JavaExecutor.executor().main().returnExpression(collectorVarName + ".build()");
    }

    @When("{string} collect with the following properties:")
    public void collectWithTheFollowingProperties(String collectorVarName,
                                                  String expressionForBuild) {
        String expressionForBuildVarName = "exp";
        JavaExecutor.executor().main().addArg(expressionForBuildVarName, expressionForBuild);
        JavaExecutor.executor().main().addRegisters(String.format("Accessors.get((String)args.get(\"%s\")).from(%s)", expressionForBuildVarName, collectorVarName));
        JavaExecutor.executor().main().returnExpression(collectorVarName);
    }
}
