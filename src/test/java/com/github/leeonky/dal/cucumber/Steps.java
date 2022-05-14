package com.github.leeonky.dal.cucumber;

import com.github.leeonky.util.Suppressor;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.SneakyThrows;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.zip.ZipOutputStream;

import static com.github.leeonky.dal.Assertions.expect;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Steps {

    private AssertionError assertionError;

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
    public void zipFile(String fileName, DataTable files) {
        ZipFile zipFile = new ZipFile(fileName);
        files.asLists().stream().map(list -> list.get(0)).forEach(fileNameInZip -> Suppressor.run(() -> {
            File file = new File(fileNameInZip);
            if (file.isDirectory())
                zipFile.addFolder(file);
            else
                zipFile.addFile(file);
        }));
    }
}
