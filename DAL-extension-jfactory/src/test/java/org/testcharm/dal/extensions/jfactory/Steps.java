package org.testcharm.dal.extensions.jfactory;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.testcharm.dal.DAL;
import org.testcharm.dal.extensions.Beans;
import org.testcharm.dal.extensions.Orders;
import org.testcharm.jfactory.JFactory;
import org.testcharm.jfactory.cucumber.JData;

import static org.testcharm.dal.Assertions.expect;

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
        expect(DAL.getInstance().getRuntimeContextBuilder().build(jFactory).getThis().dump()).should(docString);
    }
}
