package org.testcharm.pf.cucumber;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.microsoft.playwright.BrowserType;
import de.neuland.pug4j.Pug4J;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.javalin.Javalin;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcharm.dal.DAL;
import org.testcharm.interpreter.InterpreterException;
import org.testcharm.io.TempDirectory;
import org.testcharm.jfactory.JFactory;
import org.testcharm.pf.Element;
import org.testcharm.pf.PlaywrightPageFlow;
import org.testcharm.pf.SeleniumPageFlow;
import org.testcharm.util.JavaExecutor;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.testcharm.dal.Assertions.expect;
import static org.testcharm.util.Sneaky.sneakyGet;

public class Steps {
    private Throwable lastError;
    private Javalin javalin;
    private PlaywrightPageFlow.Builder<?, ?> pBuilder;
    private SeleniumPageFlow.Builder<?, ?> sBuilder;
    private final Selenium.BrowserSelenium browserSelenium = new Selenium.BrowserSelenium(
            sneakyGet(() -> {
                DesiredCapabilities capabilities = DesiredCapabilities.chrome();
                capabilities.setCapability("goog:chromeOptions", new HashMap<String, Object>() {{
                    put("prefs", new HashMap<String, Object>() {{
                        put("download.default_directory", SWorkingDir.remote().toString());
                        put("download.prompt_for_download", false);
                        put("download.directory_upgrade", true);
                        put("safebrowsing.enabled", true);
                    }});
                }});
                return new RemoteWebDriver(new URL("http://www.s.com:4444"), capabilities);
            }));

    private static final Playwright.BrowserContextPlaywright BROWSER_CONTEXT_PLAYWRIGHT = new Playwright.BrowserContextPlaywright(playwright ->
            playwright.chromium().connect("ws://www.s.com:3000/", new BrowserType.ConnectOptions().setHeaders(
                    new HashMap<String, String>() {{
                        put("x-playwright-launch-options", "{ \"headless\": false }");
                    }})));
    private Selenium.SeleniumE seleniumE;
    private Playwright.PlaywrightE playwrightE;
    private Map<String, Object> lastFormData;
    private TempDirectory pWorkingDir = new TempDirectory(Paths.get("/tmp/testcharm"));
    private TempDirectory.Shared SWorkingDir = new TempDirectory.Shared(Paths.get("/tmp/testcharm"), Paths.get("/tmp/s"));

    @When("launch the following web page:")
    public void launchTheFollowingWebPage(String pug) throws IOException {
        CountDownLatch serverReadyLatch = new CountDownLatch(1);
        javalin = Javalin.create().events(event -> event.serverStarted(serverReadyLatch::countDown));
        String html = Pug4J.render(new StringReader(pug), "", new HashMap<>());
        javalin.get("/", ctx -> ctx.html(html));
        javalin.post("/submit", ctx -> {
            lastFormData = ctx.formParamMap().entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().get(0)
                    ));
            ctx.result("ok");
        });
        javalin.start(10081);
    }

    @After
    public void closeAll() {
        if (javalin != null) {
            javalin.close();
            javalin = null;
        }
        browserSelenium.destroy();
        BROWSER_CONTEXT_PLAYWRIGHT.destroy();
        lastError = null;
        seleniumE = null;
        playwrightE = null;
    }

    @AfterAll
    public static void before_or_after_all() {
        BROWSER_CONTEXT_PLAYWRIGHT.destroyAll();
    }

    @Then("page in driver selenium should:")
    public void pageInDriverSeleniumShould(String expression) {
        expect(rootSeleniumElement()).should(expression);
    }

    private Selenium.SeleniumE rootSeleniumElement() {
        if (seleniumE == null) {
            seleniumE = browserSelenium.open("http://host.docker.internal:10081", sBuilder);
        }
        return seleniumE;
    }

    @Then("page in driver playwright should:")
    public void pageInDriverPlaywrightShould(String expression) {
        expect(rootPlaywrightElement()).should(expression);
    }

    private Playwright.PlaywrightE rootPlaywrightElement() {
        if (playwrightE == null)
            playwrightE = BROWSER_CONTEXT_PLAYWRIGHT.open("http://host.docker.internal:10081", pBuilder);
        return playwrightE;
    }

    private final TestLogger logger = TestLoggerFactory.getTestLogger(Element.class);

    @Before
    public void flushLog() {
        logger.clearAll();
        JavaExecutor.executor().importDependency(
                "org.testcharm.jfactory.*"
        );
        pBuilder = PlaywrightPageFlow.builder().workingDir(pWorkingDir.clean());
        sBuilder = SeleniumPageFlow.builder().workingDir(SWorkingDir.clean());
    }

    @And("logs should:")
    public void logsShould(String expression) {
        expect(logger.getAllLoggingEvents()).should(expression);
    }

    @When("try to find element via driver playwright:")
    public void findElementViaDriverPlaywright(String expression) {
        try {
            DAL.dal().evaluate(rootPlaywrightElement(), expression);
        } catch (Throwable e) {
            lastError = e;
        }
    }

    @When("try to find element via driver selenium:")
    public void findElementViaDriverSelenium(String expression) {
        try {
            DAL.dal().evaluate(rootSeleniumElement(), expression);
        } catch (Throwable e) {
            lastError = e;
        }
    }

    @Then("failed with:")
    public void failedWith(String message) {
        expect(lastError.getMessage().replaceAll("\\(@.*\\)", "")).isEqualTo(message);
    }

    @When("perform via driver selenium:")
    public void perform_via_driver_selenium(String actions) {
        try {
            DAL.dal().evaluateAll(rootSeleniumElement(), actions);
        } catch (InterpreterException e) {
            String detailMessage = "\n" + e.show(actions) + "\n\n" + e.getMessage();
            throw new AssertionError(detailMessage);
        }
    }

    @When("perform via driver playwright:")
    public void perform_via_driver_playwright(String actions) {
        try {
            DAL.dal().evaluateAll(rootPlaywrightElement(), actions);
        } catch (InterpreterException e) {
            String detailMessage = "\n" + e.show(actions) + "\n\n" + e.getMessage();
            throw new AssertionError(detailMessage);
        }
    }

    @Then("server should receive form data:")
    public void serverShouldReceiveFormData(String exp) {
        expect(lastFormData).should(exp);
    }

    @When("perform page {string} via driver playwright:")
    @When("perform page {string} via driver Playwright:")
    public void performPageViaDriverDriverPlaywright(String page, String exp) {
        JavaExecutor.executor().main().addArg("element", rootPlaywrightElement());
        JavaExecutor.executor().main().returnExpression("new " + page + "((org.testcharm.pf.cucumber.Playwright.PlaywrightE)args.get(\"element\"))");
        expect(JavaExecutor.executor().main().evaluate()).should(exp);
    }

    @When("perform page {string} via driver selenium:")
    @When("perform page {string} via driver Selenium:")
    public void performPageViaDriverDriverSelenium(String page, String exp) {
        JavaExecutor.executor().main().addArg("element", rootSeleniumElement());
        JavaExecutor.executor().main().returnExpression("new " + page + "((org.testcharm.pf.cucumber.Selenium.SeleniumE)args.get(\"element\"))");
        expect(JavaExecutor.executor().main().evaluate()).should(exp);
    }

    @And("register {string} with:")
    public void registerWith(String jfVar, String expression) {
        JavaExecutor.executor().main().addDeclarations(String.format("JFactory %s = new JFactory()", jfVar));
        JavaExecutor.executor().main().addRegisters(expression);

        sBuilder.jFactory((JFactory) JavaExecutor.executor().main().field(jfVar));
        pBuilder.jFactory((JFactory) JavaExecutor.executor().main().field(jfVar));
    }

    @Then("working dir should:")
    public void workingDirShould(String expression) {
        expect(Paths.get("/tmp/testcharm")).should(expression);
    }
}
