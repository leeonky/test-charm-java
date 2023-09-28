package com.github.leeonky.dal.extensions.jdbc;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.github.leeonky.dal.Assertions.expect;

public class DBSteps {
    private DataBaseBuilder builder;
    private final Connection connection = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");

    public DBSteps() throws SQLException {
    }

    @SneakyThrows
    @Before
    public void reBuild() {
        builder = new DataBaseBuilder();
        connection.createStatement().execute("delete from order_lines");
        connection.createStatement().execute("delete from products");
        connection.createStatement().execute("delete from orders");
    }

    @SneakyThrows
    @Given("all follow tables:")
    public void allFollowTables(DataTable tables) {
        builder.tableQuerier(statement -> tables.asList());
    }

    @Then("db should:")
    public void dbShould(String expression) {
        expect(builder.connect(connection)).should(expression);
    }

    @Given("all follow views:")
    public void allFollowViews(DataTable views) {
        builder.viewQuerier(statement -> views.asList());
    }
}
