package com.github.leeonky.jfactory.spec.core;

import com.github.leeonky.jfactory.JFactory;
import org.junit.jupiter.api.Test;

import static com.github.leeonky.dal.Assertions.expect;
import static com.github.leeonky.dal.Assertions.expectRun;

class SpecValue {

    @Test
    void define_spec_default_value() {
        JFactory jFactory = new JFactory();

        jFactory.factory(Bean.class).spec(spec -> spec.property("str").value("hello_" + spec.instance().getSequence()));

        expect(jFactory.create(Bean.class)).should("str= hello_1");
    }

    @Test
    void define_spec_value_with_lambda() {
        JFactory jFactory = new JFactory();

        jFactory.factory(Bean.class).spec(spec -> spec.property("str").value(() -> "from_lambda"));

        expect(jFactory.create(Bean.class)).should("str= from_lambda");
    }

    @Test
    void do_not_allow_define_sub_property_spec_value() {
        JFactory jFactory = new JFactory();

        jFactory.factory(BeanHolder.class).spec(spec -> spec.property("bean.str").value("hello"));

        expectRun(() -> jFactory.create(BeanHolder.class)).should("::throw.message: 'Property chain `bean.str` is not supported in the current operation'");
    }

    public static class Bean {
        public String str;
    }

    public static class BeanHolder {
        public Bean bean;
    }
}
