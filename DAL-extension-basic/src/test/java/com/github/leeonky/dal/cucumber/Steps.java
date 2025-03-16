package com.github.leeonky.dal.cucumber;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.Diff;
import com.github.leeonky.dal.extensions.basic.sftp.util.SFtp;
import com.github.leeonky.dal.extensions.basic.sync.Await;
import com.github.leeonky.dal.extensions.basic.sync.Eventually;
import com.github.leeonky.dal.extensions.basic.sync.Retryer;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.util.Converter;
import com.github.leeonky.util.JavaCompiler;
import com.github.leeonky.util.JavaCompilerPool;
import com.github.leeonky.util.Sneaky;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import net.lingala.zip4j.ZipFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

import static com.github.leeonky.dal.Assertions.expect;
import static com.github.leeonky.dal.extensions.basic.text.Methods.json;
import static com.github.leeonky.dal.extensions.basic.zip.Methods.unzip;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Steps {
    private AssertionError assertionError;
    private Map<String, String> sshConfig;
    private Object input;
    private DAL dal = new DAL().extend();

    private JavaCompiler javaCompiler;
    private static final JavaCompilerPool JAVA_COMPILER_POOL = new JavaCompilerPool(2, "src.test.generate.ws");

    @Before
    public void reset() {
        assertionError = null;
        sshConfig = new HashMap<>();
        input = null;
        dal = new DAL();
        dal.getRuntimeContextBuilder().setConverter(Converter.createDefault().extend());
        dal.extend();
        javaCompiler = JAVA_COMPILER_POOL.take();
        Eventually.setDefaultWaitingTime(5000);
        Await.setDefaultWaitingTime(5000);
        Retryer.setDefaultTimeout(36000 * 1000);
    }

    @SneakyThrows
    @Given("root folder {string}")
    public void root_folder_tmp_test_dir(String pathString) {
        Path path = Paths.get(pathString);
        if (path.toFile().exists())
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        path.toFile().mkdirs();
    }

    @SneakyThrows
    @Given("a file {string}")
    public void a_file(String pathString, String docString) {
        Files.write(Paths.get(pathString), docString.getBytes());
    }

    @Then("java.io.File {string} should:")
    public void java_io_file_should(String string, String expression) {
        expect(Paths.get(string).toFile()).use(dal).should(expression);
    }

    @Given("a folder {string}")
    public void a_folder(String path) {
        Paths.get(path).toFile().mkdirs();
    }

    @Then("java.io.File {string} should failed:")
    public void javaIoFileShouldFailed(String string, String expression) {
        assertionError = assertThrows(AssertionError.class, () -> expect(Paths.get(string).toFile()).use(dal).should(expression));
    }

    @And("error message should be:")
    public void errorMessageShouldBe(String message) {
        assertThat(assertionError.getMessage()).contains(message);
    }

    @Then("the following should pass:")
    public void theFollowingShouldPass(String expression) {
        expect(input).use(dal).should(expression);
    }

    @Then("java.nio.Path {string} should:")
    public void javaNioPathShould(String string, String expression) {
        expect(Paths.get(string)).use(dal).should(expression);
    }

    @Then("java.nio.Path {string} should failed:")
    public void javaNioPathShouldFailed(String string, String expression) {
        assertionError = assertThrows(AssertionError.class, () -> expect(Paths.get(string)).use(dal).should(expression));
    }

    @SneakyThrows
    @Given("an empty zip file {string}")
    public void anEmptyZipFile(String fileName) {
        FileOutputStream fos = new FileOutputStream(fileName);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        zipOut.close();
        fos.close();
    }

    @And("a zip file {string}:")
    @SneakyThrows
    public void zipFile(String fileName, DataTable files) {
        try (ZipFile zipFile = new ZipFile(fileName)) {
            files.asLists().stream().map(list -> list.get(0)).forEach(fileNameInZip -> Sneaky.run(() -> {
                File file = new File(fileNameInZip);
                if (file.isDirectory())
                    zipFile.addFolder(file);
                else
                    zipFile.addFile(file);
            }));
        }
    }

    @Given("string {string} should:")
    public void string_should(String input, String verification) {
        expect(input).use(dal).should(verification);
    }

    private static SFtp sFtp;

    @Given("ssh server on path {string}:")
    public void ssh_server(String path, io.cucumber.datatable.DataTable dataTable) {
        sshConfig = dataTable.asMaps().get(0);
        sFtp = new SFtp(sshConfig.get("host"), sshConfig.get("port"), sshConfig.get("user"), sshConfig.get("password"), path);
    }

    @Then("got sftp:")
    public void gotSftp(String expression) {
        expect(sFtp).use(dal).should(expression);
    }

    @When("evaluate sftp:")
    public void evaluateSftp(String expression) {
        try {
            gotSftp(expression);
        } catch (AssertionError e) {
            assertionError = e;
        }
    }

    @Then("java.io.File {string} should dump:")
    public void javaIoFileShouldDump(String path, String content) {
        DALRuntimeContext runtimeContext = DAL.getInstance().getRuntimeContextBuilder().build(null);

        assertThat(runtimeContext.wrap(new File(path)).dump()).isEqualTo(content);
    }

    @SneakyThrows
    @And("set file attribute {string}")
    public void setFileAttribute(String pathStr, String attribute) {
        String[] attributes = attribute.split(" ");
        Path path = Paths.get(pathStr);
        Files.setLastModifiedTime(path, FileTime.from(Instant.parse(attributes[3])));
        Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(attributes[0]));
        Runtime.getRuntime().exec("sudo chown " + attributes[1] + ":" + attributes[2] + " " + path).waitFor();
    }

    @Then("java.io.path {string} should dump:")
    public void javaIoPathShouldDump(String path, String content) {
        DALRuntimeContext runtimeContext = DAL.getInstance().getRuntimeContextBuilder().build(null);

        assertThat(runtimeContext.wrap(Paths.get(path)).dump()).isEqualTo(content);
    }

    @Then("sftp {string} should dump:")
    public void sftpShouldDump(String path, String content) {
        DALRuntimeContext runtimeContext = DAL.getInstance().getRuntimeContextBuilder().build(null);
        if (sFtp != null)
            sFtp.close();
        sFtp = new SFtp(sshConfig.get("host"), sshConfig.get("port"), sshConfig.get("user"), sshConfig.get("password"), path);
        assertThat(runtimeContext.wrap(sFtp).dump()).isEqualTo(content);
    }

    @SneakyThrows
    @Then("zip file {string} should dump:")
    public void zipFileShouldDump(String path, String content) {
        DALRuntimeContext runtimeContext = DAL.getInstance().getRuntimeContextBuilder().build(null);

        assertThat(runtimeContext.wrap(unzip(Files.readAllBytes(Paths.get(path)))).dump()).isEqualTo(content);
    }

    @After
    public void closeFtp() {
        if (sFtp != null)
            sFtp.close();
        JAVA_COMPILER_POOL.giveBack(javaCompiler);
    }


    @Given("the following json:")
    public void theFollowingJson(String json) {
        input = json(json);
    }

    @When("evaluate by:")
    public void evaluateBy(String expression) {
        try {
            expect(input).use(dal).should(expression);
        } catch (AssertionError e) {
            assertionError = e;
        }
    }

    @Then("failed with the message:")
    public void failedWithTheMessage(String message) {
        assertThat(assertionError.getMessage()).isEqualTo(message.replace("#package#", javaCompiler.packagePrefix()));
    }

    private String left, right;

    @Given("the left side:")
    public void theLeftSide(String content) {
        left = content;
    }

    @Given("the right side:")
    public void theRightSide(String content) {
        right = content;
    }

    @Then("the diff should be:")
    public void theDiffShouldBe(String result) {
        assertThat(new Diff("Diff:\nExpect:", left, right).detail()).isEqualTo(result);
    }

    @Given("a class object with string {string} and can be converted to bytes")
    public void aClassObjectWithStringAndCanBeConvertedToBytes(String value) {
        input = new ToBytes(value);
        dal.getRuntimeContextBuilder().getConverter().addTypeConverter(ToBytes.class, byte[].class, ToBytes::getBytes);
    }

    @Given("a class object with string {string} and can be converted to input-stream")
    public void aClassObjectWithStringAndCanBeConvertedToInputStream(String value) {
        input = new ToBytes(value);
        dal.getRuntimeContextBuilder().getConverter().addTypeConverter(ToBytes.class, InputStream.class, ToBytes::getInputStream);
    }

    @Given("a list from {int} to n")
    public void aListFromToN(int start) {
        input = Stream.iterate(1, i -> i + 1);
    }

    @SneakyThrows
    @Given("the following java class:")
    public void theFollowingJavaClass(String code) {
        input = javaCompiler.compileToClasses(Collections.singletonList(
                "import com.github.leeonky.dal.*;\n" +
                        "import com.github.leeonky.dal.type.*;\n" +
                        "import com.github.leeonky.util.*;\n" +
                        "import com.github.leeonky.dal.runtime.*;\n" +
                        "import java.util.*;\n" +
                        "import java.util.function.*;\n" +
                        "import java.time.*;\n" +
                        "import com.github.leeonky.dal.extensions.basic.sync.*;\n" +
                        "import java.math.*;\n" + code)).get(0).newInstance();
    }

    public static class ToBytes {
        private final String value;

        public ToBytes(String value) {
            this.value = value;
        }

        public byte[] getBytes() {
            return value.getBytes();
        }

        public InputStream getInputStream() {
            return new ByteArrayInputStream(getBytes());
        }
    }
}
