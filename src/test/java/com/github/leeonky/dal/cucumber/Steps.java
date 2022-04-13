package com.github.leeonky.dal.cucumber;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static com.github.leeonky.dal.Assertions.expect;

public class Steps {

    @Before
    public void cleanDir() {

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
        Path path = Paths.get(pathString);
        if (path.toFile().exists())
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        Files.write(path, docString.getBytes());
    }

    @Then("java.io.File {string} should:")
    public void java_io_file_should(String string, String expression) {
        expect(Paths.get(string).toFile()).should(expression);
    }
}
