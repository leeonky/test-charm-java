package com.github.leeonky.dal.extensions.jfactory;

import com.github.leeonky.dal.extensions.Orders;
import com.github.leeonky.jfactory.JFactory;
import com.github.leeonky.jfactory.cucumber.JData;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static com.github.leeonky.dal.Assertions.expect;

public class Steps {

    private static final JFactory jFactory = new JFactory() {{
        register(Orders.class);
    }};
    private static final JData jdata = new JData(jFactory);

    @Given("{string}:")
    public void givenData(String spec, io.cucumber.datatable.DataTable dataTable) {
        jdata.prepare(spec, dataTable.asMaps());
    }

    @Then("query data by jfactory:")
    public void query_data_by_jfactory(String dal) {
        expect(jFactory).should(dal);
    }
}
