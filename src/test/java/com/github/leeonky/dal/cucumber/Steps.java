package com.github.leeonky.dal.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

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
        assertThat(assertionError.getMessage()).isEqualTo(message);
    }

    @Then("the following should pass:")
    public void theFollowingShouldPass(String expression) {
        expect(expression).should(expression);
    }
}
