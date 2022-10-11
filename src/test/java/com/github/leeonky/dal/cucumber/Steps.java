package com.github.leeonky.dal.cucumber;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.SFtp;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.util.Suppressor;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import static com.github.leeonky.dal.Assertions.expect;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Steps {

    private AssertionError assertionError;
    private Map<String, String> sshConfig;

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
        expect(Paths.get(string).toFile()).should(expression);
    }

    @Given("a folder {string}")
    public void a_folder(String path) {
        Paths.get(path).toFile().mkdirs();
    }

    @Then("java.io.File {string} should failed:")
    public void javaIoFileShouldFailed(String string, String expression) {
        assertionError = assertThrows(AssertionError.class, () -> expect(Paths.get(string).toFile()).should(expression));
    }

    @And("error message should be:")
    public void errorMessageShouldBe(String message) {
        assertThat(assertionError.getMessage()).contains(message);
    }

    @Then("the following should pass:")
    public void theFollowingShouldPass(String expression) {
        expect(expression).should(expression);
    }

    @Then("java.nio.Path {string} should:")
    public void javaNioPathShould(String string, String expression) {
        expect(Paths.get(string)).should(expression);
    }

    @Then("java.nio.Path {string} should failed:")
    public void javaNioPathShouldFailed(String string, String expression) {
        assertionError = assertThrows(AssertionError.class, () -> expect(Paths.get(string)).should(expression));
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
            files.asLists().stream().map(list -> list.get(0)).forEach(fileNameInZip -> Suppressor.run(() -> {
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
        expect(input).should(verification);
    }

    private static SFtp sFtp;

    @Given("ssh server on path {string}:")
    public void ssh_server(String path, io.cucumber.datatable.DataTable dataTable) {
        sshConfig = dataTable.asMaps().get(0);
        sFtp = new SFtp(sshConfig.get("host"), sshConfig.get("port"), sshConfig.get("user"), sshConfig.get("password"), path);
    }

    @Then("got sftp:")
    public void gotSftp(String expression) {
        expect(sFtp).should(expression);
    }

    @When("evaluate sftp:")
    public void evaluateSftp(String expression) {
        try {
            gotSftp(expression);
        } catch (AssertionError e) {
            assertionError = e;
        }
    }

    @Then("got failed message:")
    public void gotFailedMessage() {
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

    @After
    void closeFtp() {
        if (sFtp != null)
            sFtp.close();
    }
}
