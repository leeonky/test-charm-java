package com.github.leeonky.jfactory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.leeonky.dal.Assertions.expect;
import static com.github.leeonky.jfactory.JFactoryPropertyParser.given;

public class JFactoryPropertyParserTest {

    public static class Bean {
        public String value;
    }

    public static class BeanObject {
        public Bean bean;
        public List<Bean> beans;
    }

    @Test
    void create_with_sub_object() {
        JFactory jFactory = new JFactory();

        BeanObject beanObject = jFactory.type(BeanObject.class).propertyValue("bean", given("value= hello")).create();

        expect(beanObject).should("bean.value= hello");
    }

    @Test
    void create_sub_array() {
        JFactory jFactory = new JFactory();

        BeanObject beanObject = jFactory.type(BeanObject.class).propertyValue("beans", given("[{value= hello} {value=world}]")).create();

        expect(beanObject).should("beans.value[]= [hello world]");
    }

    @Test
    void create_array() {
        JFactory jFactory = new JFactory();

        Bean[] beans = jFactory.type(Bean[].class).properties(given("[{value= hello} {value=world}]")).create();

        expect(beans).should("value[]= [hello world]");
    }
}