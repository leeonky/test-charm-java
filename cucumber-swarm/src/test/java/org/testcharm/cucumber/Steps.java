package org.testcharm.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testcharm.cucumber.swarm.Main;
import org.testcharm.dal.DAL;
import org.testcharm.io.TempDirectory;
import org.testcharm.util.JavaExecutor;
import org.testcharm.util.Sneaky;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testcharm.dal.Assertions.expect;
import static org.testcharm.dal.extensions.basic.binary.BinaryExtension.readAllAndClose;
import static org.testcharm.dal.extensions.basic.string.Methods.string;

public class Steps {
    TempDirectory globalTempDirectory, cucumberDirectory, featuresDirectory;
    private final static AtomicInteger SWARM_PORT = new AtomicInteger(10084);
    private Process process;

    @Before
    public void clean() {
        globalTempDirectory = new TempDirectory(Paths.get("src", "test", "generate")).mkdir(JavaExecutor.executor().compiler().getLocation().getName());
        globalTempDirectory.clean();
        cucumberDirectory = globalTempDirectory.mkdir("cucumber");
        featuresDirectory = cucumberDirectory.mkdir("features");

        JavaExecutor.executor().resetAll();
    }

    @When("run cucumber with the following args:")
    public void run_cucumber_with_the_following_args(String docString) throws IOException {
        JavaExecutor.executor().main().evaluate();

        List<String> args = new ArrayList<>();
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classpath = String.join(File.pathSeparator, System.getProperty("java.class.path").split(File.pathSeparator));
        classpath += File.pathSeparator + JavaExecutor.executor().compiler().getLocation().getAbsolutePath();
        args.add(javaBin);
        args.add("-cp");
        args.add(classpath);
        args.add(Main.class.getName());
        args.add("--swarm-port");
        args.add(String.valueOf(SWARM_PORT.getAndIncrement()));
        DAL.dal().evaluateAll(null, docString, new HashMap<String, String>() {{
            put("path", cucumberDirectory.root().toString() + File.separator);
        }}).forEach(e -> args.add(String.valueOf(e)));
        process = new ProcessBuilder(args.toArray(new String[0])).start();
    }

    @Then("the task result should be:")
    public void the_output_should(String docString) {
        expect(new HashMap<String, Object>() {{
            put("code", Sneaky.get(process::waitFor));
            put("stdout", string(readAllAndClose(process.getInputStream())));
            put("stderr", string(readAllAndClose(process.getErrorStream())));
        }}).should(docString.replace("$path$", cucumberDirectory.root().toAbsolutePath().toString()));
    }

    @After
    public void after() {
        if (process != null)
            process.destroyForcibly();
    }

    @Given("the feature file {string}:")
    public void theFeatureFile(String path, String content) {
        featuresDirectory.write(path, content);
    }
}
