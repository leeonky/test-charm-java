package com.github.leeonky.jfactory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.leeonky.dal.Assertions.expect;

class TestConsistentListWithSingle {
    private final JFactory jFactory = new JFactory();

    @Test
    void single_effect_list() {
        jFactory.factory(BeanList.class).spec(ins -> {
            Spec<BeanList> spec = ins.spec();
            spec.consistent(String.class)
                    .list("beans").consistent(c -> c.direct("status"))
                    .direct("status");
        });

        BeanList beanList = jFactory.type(BeanList.class)
                .property("beans[0]!", null)
                .property("beans[1]!", null)
                .property("status", "new").create();

        expect(beanList).should("<<beans[0].status, beans[1].status, status>>= new");
    }

    @Test
    void single_effect_list_use_reader_writer() {
        jFactory.factory(BeanList.class).spec(ins -> {
            Spec<BeanList> spec = ins.spec();
            spec.consistent(Object.class)
                    .list("beans").consistent(c -> c
                            .property("status").read(s -> s).write(s -> s))
                    .direct("status");
        });

        BeanList beanList = jFactory.type(BeanList.class)
                .property("beans[0]!", null)
                .property("beans[1]!", null)
                .property("status", "new").create();

        expect(beanList).should("<<beans[0].status, beans[1].status, status>>= new");
    }

    public static class Bean {
        public String status;
    }

    public static class BeanList {
        public List<Bean> beans;
        public String status;
    }
}
