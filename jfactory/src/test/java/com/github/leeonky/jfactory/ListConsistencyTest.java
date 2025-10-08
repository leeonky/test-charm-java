package com.github.leeonky.jfactory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.leeonky.dal.Assertions.expect;
import static com.github.leeonky.jfactory.Coordinate.d1;
import static com.github.leeonky.jfactory.Normalizer.reverse;
import static com.github.leeonky.jfactory.Normalizer.shift;

class ListConsistencyTest {

    public static class Bean {
        public String status1, status2, status3;
    }

    public static class BeanList {
        public List<Bean> beans1, beans2;
    }

    @Test
    void reverse_index() {

        JFactory jFactory = new JFactory();

        jFactory.factory(BeanList.class).spec(ins -> {
            ins.spec().structure(Coordinate.D2.class)
                    .list("beansList1", "beans").normalize(d2 -> Coordinate.d2(d2.index1(), d2.index0()),
                            d2 -> Coordinate.d2(d2.index1(), d2.index0()))
                    .list("beansList2", "beans");
        });

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
    void reverse_index_2() {
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

    @Test
    void change_index() {
        JFactory jFactory = new JFactory();

        jFactory.factory(BeanList.class).spec(ins ->
                ins.spec().consistent(String.class)
                        .list("beans1").normalize(shift(1))
                        .consistent(beans1 -> beans1
                                .direct("status1"))
                        .list("beans2").consistent(beans2 -> beans2
                                .direct("status1")));

        BeanList beanList = jFactory.clear().type(BeanList.class)
                .property("beans1[0]!.status1", "a")
                .property("beans1[1]!", "")
                .property("beans1[2]!.status1", "c")
                .property("beans2[0]!", "")
                .property("beans2[1]!", "")
                .property("beans2[2]!.status1", "b")
                .create();

        expect(beanList).should("beans1.status1[]= [a b c]");
        expect(beanList).should("beans2.status1[]= [c a b]");
    }

    @Test
    void change_index_2() {
        JFactory jFactory = new JFactory();

        jFactory.factory(BeanList.class).spec(ins ->
                ins.spec().consistent(String.class, Coordinate.D1.class)
                        .list("beans1").normalize(d1 -> d1(d1.index().shift(1)), d1 -> d1(d1.index().shift(-1)))
                        .consistent(beans1 -> beans1
                                .direct("status1"))
                        .list("beans2").consistent(beans2 -> beans2
                                .direct("status1")));

        BeanList beanList = jFactory.clear().type(BeanList.class)
                .property("beans1[0]!.status1", "a")
                .property("beans1[1]!", "")
                .property("beans1[2]!.status1", "c")
                .property("beans2[0]!", "")
                .property("beans2[1]!", "")
                .property("beans2[2]!.status1", "b")
                .create();

        expect(beanList).should("beans1.status1[]= [a b c]");
        expect(beanList).should("beans2.status1[]= [c a b]");
    }
}
