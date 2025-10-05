package com.github.leeonky.jfactory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.leeonky.dal.Assertions.expect;
import static com.github.leeonky.jfactory.Coordinate.d1;
import static com.github.leeonky.jfactory.Normalizer.reverse;

class ListConsistencyTest {

    public static class Bean {
        public String status1, status2, status3;
    }

    public static class BeanList {
        public List<Bean> beans1, beans2;
    }

    @Test
    public void reverse_index() {
        JFactory jFactory = new JFactory();

        jFactory.factory(BeanList.class).spec(ins ->
                ins.spec().consistent(String.class)
                        .list("beans1").normalize(reverse())
                        .consistent(beans1 -> beans1
                                .direct("status1"))
                        .list("beans2").consistent(beans2 -> beans2
                                .direct("status1")));

        BeanList beanList = jFactory.clear().type(BeanList.class)
                .property("beans1[0]!.status1", "a")
                .property("beans1[1]!.status1", "b")
                .property("beans2[0]!", "")
                .property("beans2[1]!", "")
                .create();

        expect(beanList).should("beans1.status1[]= [a b]");
        expect(beanList).should("beans2.status1[]= [b a]");
    }

    @Test
    public void reverse_index_2() {
        JFactory jFactory = new JFactory();

        jFactory.factory(BeanList.class).spec(ins ->
                ins.spec().consistent(String.class, Coordinate.D1.class)
                        .list("beans1").normalize(d1 -> d1(d1.index().reverse()), d1 -> d1(d1.index().reverse()))
                        .consistent(beans1 -> beans1
                                .direct("status1"))
                        .list("beans2").consistent(beans2 -> beans2
                                .direct("status1")));

        BeanList beanList = jFactory.clear().type(BeanList.class)
                .property("beans1[0]!.status1", "a")
                .property("beans1[1]!.status1", "b")
                .property("beans2[0]!", "")
                .property("beans2[1]!", "")
                .create();

        expect(beanList).should("beans1.status1[]= [a b]");
        expect(beanList).should("beans2.status1[]= [b a]");
    }
}
