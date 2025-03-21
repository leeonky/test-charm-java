package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.DAL;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.TimeZone;

import static com.github.leeonky.dal.Assertions.expect;

public class DBSteps {
    private DataBaseBuilder builder;
    private final Connection connection = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
    private Throwable exception;

    public DBSteps() throws SQLException {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @SneakyThrows
    @Before
    public void reBuild() {
        builder = new DataBaseBuilder();
        connection.createStatement().execute("delete from sku_products");
        connection.createStatement().execute("delete from order_lines");
        connection.createStatement().execute("delete from products");
        connection.createStatement().execute("delete from orders");
        connection.createStatement().execute("delete from skus");
        PicoFactory.jFactory.getDataRepository().clear();
    }

    @SneakyThrows
    @Given("all follow tables:")
    public void allFollowTables(DataTable tables) {
        builder.tablesProvider(statement -> tables.asList());
    }

    @Then("db should:")
    public void dbShould(String expression) {
        expect(builder.connect(connection)).should(expression);
    }

    @When("assert DB:")
    public void assertDB(String expression) {
        try {
            expect(builder.connect(connection)).should(expression);
        } catch (Throwable e) {
            exception = e;
        }
    }

    @Then("raise error")
    public void raiseError(String expression) {
        expect(exception).should(expression);
    }

    @And("define to to upper name method on products row")
    public void defineToToUpperNameMethodOnProductsRow() {
        builder.tableStrategy("products").registerRowMethod("upperName", row -> ((String) row.value("name")).toUpperCase());
    }

    @When("define to to hasMany skus method on products row")
    public void defineToToHasManySkusMethodOnProductsRow() {
        builder.tableStrategy("products").registerRowMethod("skus", row1 ->
                row1.hasMany("skus").on("id").through("sku_products", "sku_id").on("product_id = :id"));

        builder.tableStrategy("products").registerRowMethod("skusInSql", row ->
                row.join("skus").where("id in (select sku_id from sku_products where product_id = :id)"));
    }

    @Then("dumped data base should:")
    public void dumpedDataBaseShould(String expression) {
        expect(DAL.getInstance().getRuntimeContextBuilder().build(builder.connect(connection)).getThis().dumpValue())
                .should(expression);
    }
}
