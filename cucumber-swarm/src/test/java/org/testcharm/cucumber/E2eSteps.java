package org.testcharm.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import org.testcharm.cucumber.swarm.Main;
import org.testcharm.dal.DAL;
import org.testcharm.io.TempDirectory;
import org.testcharm.message.MessageConverterRegistry;
import org.testcharm.util.JavaExecutor;
import org.testcharm.util.Sneaky;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static org.testcharm.dal.Assertions.expect;
import static org.testcharm.dal.extensions.basic.binary.BinaryExtension.readAllAndClose;
import static org.testcharm.dal.extensions.basic.string.Methods.string;

public class E2eSteps {
    TempDirectory globalTempDirectory, cucumberDirectory, featuresDirectory;
    private final static AtomicInteger SWARM_PORT = new AtomicInteger(10084);
    private Process process;

    @Before
    public void clean() {
        globalTempDirectory = globalTempDir();
        globalTempDirectory.clean();
        cucumberDirectory = globalTempDirectory.mkdir("cucumber");
        featuresDirectory = cucumberDirectory.mkdir("features");

        cucumberDirectory.write("logging.properties", new StringBuilder()
                .append("handlers= java.util.logging.FileHandler\n")
                .append(".level= INFO\n")
                .append("java.util.logging.FileHandler.pattern = ").append(cucumberDirectory.resolve("cucumber.log")).append("\n")
                .append("java.util.logging.FileHandler.append = true\n")
                .append("java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter\n")
                .append("java.util.logging.FileHandler.level = ALL\n")
                .append("io.cucumber.level = INFO\n")
                .append("io.cucumber.core.level = INFO\n")
                .append("io.cucumber.core.runtime.WorkerRuntime.level = INFO\n")
                .append("org.testcharm.level = FINE\n")
                .toString());

        JavaExecutor.executor().resetAll();
    }

    public static TempDirectory globalTempDir() {
        return new TempDirectory(Paths.get("src", "test", "generate")).mkdir(JavaExecutor.executor().compiler().getLocation().getName());
    }

    @SneakyThrows
    @When("run cucumber with the following args:")
    public void run_cucumber_with_the_following_args(String docString) {
        JavaExecutor.executor().main().evaluate();

        List<String> args = new ArrayList<>();
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classpath = String.join(File.pathSeparator, System.getProperty("java.class.path").split(File.pathSeparator));
        classpath += File.pathSeparator + JavaExecutor.executor().compiler().getLocation().getAbsolutePath();
        args.add(javaBin);
        args.add("-Djava.util.logging.config.file=" + cucumberDirectory.resolve("logging.properties").toAbsolutePath());
        if (isDebugging())
            args.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005");
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

    public static boolean isDebugging() {
        List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String a : args) {
            if (a.contains("-agentlib:jdwp")) return true;
        }
        return false;
    }

    @Then("the task result should be:")
    public void the_output_should(String docString) {
        expect(new HashMap<String, Object>() {{
            put("code", Sneaky.get(process::waitFor));
            put("stdout", string(readAllAndClose(process.getInputStream())).replace("\t", "\\t"));
            put("stderr", string(readAllAndClose(process.getErrorStream())).replace("\t", "\\t"));
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

    @And("the log should:")
    @And("the master log should:")
    public void theLogShould(String expression) throws IOException {
        Path relativize = Paths.get(System.getProperty("user.dir")).relativize(cucumberDirectory.root().toAbsolutePath());
        expect(new String(Files.readAllBytes(cucumberDirectory.resolve("cucumber.log"))))
                .should(expression.replace("$r_path$", relativize.toString()));
    }

    @SneakyThrows
    @Then("the following event should be emitted after cucumber run:")
    public void theFollowingEventShouldBeEmittedAfterCucumberRun(String expression) {
        TempDirectory dir = E2eSteps.globalTempDir().mkdir("dal");
        dir.write("verify.dal", expression.replace("$path$", cucumberDirectory.root().toAbsolutePath().toString()).replace("\\t", "\t"));
        run_cucumber_with_the_following_args(String.join("\n", "'--plugin'", "'org.testcharm.cucumber.swarm.EventCollectorPlugin'", "'--glue'", "'steps'", "$path + 'features'"));
        process.waitFor();

        if (!dir.exist("passed")) {
            throw new AssertionError("\n" + dir.readAllText("failed"));
        }
    }

    @SneakyThrows
    @When("run cucumber in remote mode with the following args:")
    public void runCucumberInRemoteModeWithTheFollowingArgs(String docString) {
        JavaExecutor.executor().main().evaluate();

        List<String> args = new ArrayList<>();
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classpath = String.join(File.pathSeparator, System.getProperty("java.class.path").split(File.pathSeparator));
        classpath += File.pathSeparator + JavaExecutor.executor().compiler().getLocation().getAbsolutePath();
        args.add(javaBin);
        args.add("-Djava.util.logging.config.file=" + cucumberDirectory.resolve("logging.properties").toAbsolutePath());
        List<String> remoteOptions = new ArrayList<>(args);
        remoteOptions.remove(0);
        if (isDebugging()) {
            args.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005");
            remoteOptions.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5006");
        }
        args.add("-cp");
        args.add(classpath);
        args.add(Main.class.getName());

        args.add("--swarm-port");
        int port = SWARM_PORT.getAndIncrement();
        args.add(String.valueOf(port));

        remoteOptions.add("-cp");
        remoteOptions.add(classpath);
        remoteOptions.add(Main.class.getName());

        args.add("--disable-local-worker");

        DAL.dal().evaluateAll(null, docString, new HashMap<String, String>() {{
            put("path", cucumberDirectory.root().toString() + File.separator);
        }}).forEach(e -> args.add(String.valueOf(e)));


        remoteOptions.add("--plugin");
        remoteOptions.add("pretty");

        args.addAll(asList("--remote-options-json", MessageConverterRegistry.jsonConverter().serialize(remoteOptions)));
        args.addAll(asList("--remote-worker-launcher", javaBin));

        process = new ProcessBuilder(args.toArray(new String[0])).start();
    }

    @SneakyThrows
    @And("the worker {int} log should:")
    public void theWorkerLogShould(int id, String expression) {
        Path relativize = Paths.get(System.getProperty("user.dir")).relativize(cucumberDirectory.root().toAbsolutePath());
        expect(new String(Files.readAllBytes(cucumberDirectory.resolve("cucumber.log." + id))))
                .should(expression.replace("$r_path$", relativize.toString()));
    }
}
