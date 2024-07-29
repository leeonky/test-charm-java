package com.github.leeonky.dal.extensions.jfactory;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.Beans;
import com.github.leeonky.dal.extensions.Orders;
import com.github.leeonky.jfactory.JFactory;
import com.github.leeonky.jfactory.cucumber.JData;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static com.github.leeonky.dal.Assertions.expect;

public class Steps {

    private static final JFactory jFactory = new JFactory() {{
        register(Orders.class);
        register(Beans.class);
    }};
    private static final JData jdata = new JData(jFactory);

    @Before
    public void reset() {
        jFactory.getDataRepository().clear();
    }

    @Given("{string}:")
    public void givenData(String spec, io.cucumber.datatable.DataTable dataTable) {
        jdata.prepare(spec, dataTable.asMaps());
    }

    @Then("query data by jfactory:")
    public void query_data_by_jfactory(String dal) {
        expect(jFactory).should(dal);
    }

    @Then("dumped jfactoy should be:")
    public void dumped_jfactoy_should_be(String docString) {
        expect(DAL.getInstance().getRuntimeContextBuilder().build(null).wrap(jFactory).dumpAll()).should(docString);
    }
}
