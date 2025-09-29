Feature: list consistency

  Background:
    Given declaration jFactory =
    """
    new JFactory();
    """
    Given the following bean class:
    """
    public class Bean {
        public String status1, status2, status3;
    }
    """

  Rule: single and list

    Background:
      And the following bean class:
        """
        public class BeanList {
            public List<Bean> beans;
            public String status1, status2, status3;
        }
        """

    Scenario: single property effect all element property
      And operate:
        """
        jFactory.factory(BeanList.class).spec(ins -> {
            Spec<BeanList> spec = ins.spec();
            spec.consistent(String.class)
                    .list("beans").consistent(beans -> beans
                      .direct("status1"))
                    .direct("status1");
        });
        """
      When build:
        """
        jFactory.type(BeanList.class)
                .property("beans[0]!", null)
                .property("beans[1]!", null)
                .property("status1", "new").create();
        """
      Then the result should:
        """
        <<beans<<0, 1>>, ::root>>.status1= new
        """

    Scenario: single property effect element property which has default factory value
      And operate:
        """
        jFactory.factory(BeanList.class).spec(ins -> {
            Spec<BeanList> spec = ins.spec();
            spec.consistent(String.class)
                    .list("beans").consistent(beans-> beans
                      .direct("status1"))
                    .direct("status1");
        });
        """
      When build:
        """
        jFactory.type(BeanList.class)
                .property("beans[1]!", null)
                .property("status1", "new").create();
        """
      Then the result should:
        """
        : {
          <<beans[1], ::this>>.status1= new
          beans[0]: null
        }
        """

    Scenario: element impact element property
      And operate:
        """
        jFactory.factory(BeanList.class).spec(ins -> {
            Spec<BeanList> spec = ins.spec();
            spec.consistent(String.class)
                    .list("beans").consistent(beans -> beans
                      .direct("status1"))
                    .direct("status1");
        });
        """
      When build:
        """
        jFactory.type(BeanList.class)
                .property("beans[1].status1", "new")
                .create();
        """
      Then the result should:
        """
        <<beans[1], ::root>>.status1= new
        """

    Scenario: different property in list consistent
      And operate:
        """
        jFactory.factory(BeanList.class).spec(ins -> {
            Spec<BeanList> spec = ins.spec();
            spec.consistent(String.class)
                    .list("beans").consistent(beans -> beans
                      .direct("status1")
                      .direct("status2"))
                    .direct("status1");
        });
        """
      When build:
        """
        jFactory.type(BeanList.class)
                .property("beans[0]!", null)
                .property("beans[1]!", null)
                .property(".status1", "new")
                .create();
        """
      Then the result should:
        """
        : {
          <<beans[0], beans[1], ::this>>.status1= new
          <<beans[0], beans[1]>>.status2= new
        }
        """

  Rule: reader and writer in list

    Background:
      And the following bean class:
        """
        public class BeanList {
            public List<Bean> beans;
            public String status1, status2, status3;
        }
        """

    Scenario: define reader and writer in list property
      And operate:
        """
        jFactory.factory(BeanList.class).spec(ins -> {
            Spec<BeanList> spec = ins.spec();
            spec.consistent(Object.class)
                    .list("beans").consistent(beans -> beans.property("status1")
                      .read(s->s)
                      .write(s->s))
                    .direct("status1");
        });
        """
      When build:
        """
        jFactory.type(BeanList.class)
                .property("beans[0]!", null)
                .property("beans[1]!", null)
                .property("status1", "new").create();
        """
      Then the result should:
        """
        <<beans<<0, 1>>, ::root>>.status1= new
        """

    Scenario: multi properties
      And operate:
        """
        jFactory.factory(BeanList.class).spec(ins -> {
            Spec<BeanList> spec = ins.spec();
            spec.consistent(Object[].class)
                    .list("beans").consistent(beans-> beans
                      .properties("status1", "status2")
                        .read((s1,s2)->new Object[]{s1,s2})
                        .write(s->s[0], s->s[1]))
                    .properties("status1", "status2")
                      .read((s1,s2)->new Object[]{s1,s2})
                        .write(s->s[0], s->s[1]);
        });
        """
      When build:
        """
        jFactory.type(BeanList.class)
                .property("beans[0]!", null)
                .property("beans[1]!", null)
                .property("status1", "new")
                .property("status2", "world")
                .create();
        """
      Then the result should:
        """
        : {
          <<beans<<0, 1>>, ::this>>.status1= new
          <<beans<<0, 1>>, ::this>>.status2= world
        }
        """

    Scenario: three properties
      And operate:
        """
        jFactory.factory(BeanList.class).spec(ins -> {
            Spec<BeanList> spec = ins.spec();
            spec.consistent(Object[].class)
                    .list("beans").consistent(beans-> beans
                      .properties("status1", "status2", "status3")
                        .read((s1,s2,s3)->new Object[]{s1,s2,s3})
                        .write(s->s[0], s->s[1], s->s[2]))
                    .properties("status1", "status2", "status3")
                      .read((s1,s2,s3)->new Object[]{s1,s2,s3})
                        .write(s->s[0], s->s[1], s->s[2]);
        });
        """
      When build:
        """
        jFactory.type(BeanList.class)
                .property("beans[0]!", null)
                .property("beans[1]!", null)
                .property("status1", "hello")
                .property("status2", "new")
                .property("status3", "world")
                .create();
        """
      Then the result should:
        """
        : {
          <<beans<<0, 1>>, ::this>>.status1= hello
          <<beans<<0, 1>>, ::this>>.status2= new
          <<beans<<0, 1>>, ::this>>.status3= world
        }
        """

    Scenario: more properties
      And operate:
        """
        jFactory.factory(BeanList.class).spec(ins -> {
            Spec<BeanList> spec = ins.spec();
            spec.consistent(Object[].class)
                    .list("beans").consistent(beans-> beans
                      .properties("status1", "status2")
                        .read(ps -> ps)
                        .write(ps -> ps))
                    .properties("status1", "status2")
                      .read((s1,s2)->new Object[]{s1,s2})
                        .write(s->s[0], s->s[1]);
        });
        """
      When build:
        """
        jFactory.type(BeanList.class)
                .property("beans[0]!", null)
                .property("beans[1]!", null)
                .property("status1", "new")
                .property("status2", "world")
                .create();
        """
      Then the result should:
        """
        : {
          <<beans<<0, 1>>, ::this>>.status1= new
          <<beans<<0, 1>>, ::this>>.status2= world
        }
        """

  Rule: list and list

    Background:
      And the following bean class:
        """
        public class BeanList {
            public List<Bean> beans1, beans2;
            public String status1, status2, status3;
        }
        """

    Scenario: list effect list property
      And operate:
        """
        jFactory.factory(BeanList.class).spec(ins -> {
            ins.spec().consistent(String.class)
                    .list("beans1").consistent(beans -> beans
                      .direct("status1"))
                    .list("beans2").consistent(beans -> beans
                      .direct("status1"))
                    .direct("status1");
        });
        """
      When build:
        """
        jFactory.clear().type(BeanList.class)
                .property("beans1[0]!", null)
                .property("beans1[1]!", null)
                .property("beans2[0]!", null)
                .property("beans2[1]!", null)
                .property("status1", "new").create();
        """
      Then the result should:
        """
        <<beans1<<0, 1>>, beans2<<0, 1>>, ::root>>.status1= new
        """

#TODO multi list in one consistent
#TODO reverseAssociations
#TODO nested list consistent
