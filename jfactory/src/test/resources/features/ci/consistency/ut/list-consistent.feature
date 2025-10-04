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

  Rule: between list element

    Background:
      Given the following bean class:
      """
      public class Bean {
        public String status1, status2;
      }
      """
      Given the following bean class:
      """
      public class Beans {
        public Bean[] beans;
        public String status1, status2;
      }
      """

    Scenario: with out default collection element. provider not work
      And the following spec class:
        """
        public class BeansSpec extends Spec<Beans> {
          public void main() {
            property("status1").dependsOn("beans[0].status1");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(BeansSpec.class).create();
        """
      Then the result should:
        """
        : {beans: null, status1: /^status1.*/}
        """

    Scenario: with out default collection element. provider and consumer not work
      And the following spec class:
        """
        public class BeansSpec extends Spec<Beans> {
          public void main() {
            property("beans[1].status1").dependsOn("beans[0].status1");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(BeansSpec.class).create();
        """
      Then the result should:
        """
        : {beans: null}
        """

    Scenario: with out default collection element. consumer not work
      And the following spec class:
        """
        public class BeansSpec extends Spec<Beans> {
          public void main() {
            link("beans[1].status1", "status1");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(BeansSpec.class).create();
        """
      Then the result should:
        """
        : {
          beans: [null, {status1= /^status1.*/}]
          status1= .beans[1].status1
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

    Scenario: single and multi list
      And the following bean class:
        """
        public class BeanList {
            public List<Bean> beans1, beans2;
            public String status1, status2, status3;
        }
        """
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

    Scenario: list beans1[0] -> beans2[0], beans2[1] -> beans1[1]
      And operate:
            """
            jFactory.factory(BeanList.class).spec(ins -> {
                ins.spec().consistent(String.class)
                    .list("beans1").consistent(beans1 -> beans1
                      .direct("status1"))
                    .list("beans2").consistent(beans2 -> beans2
                      .direct("status1"));
            });
            """
      When build:
            """
            jFactory.clear().type(BeanList.class)
                    .property("beans1[0]!.status1", "a")
                    .property("beans1[1]!", "")
                    .property("beans2[0]!", "")
                    .property("beans2[1]!.status1", "b")
                    .create();
            """
      Then the result should:
            """
            : {
              <<beans1[0], beans2[0]>>.status1= a
              <<beans1[1], beans2[1]>>.status1= b
            }
            """

  Rule: nested list

    Background:
      And the following bean class:
        """
        public class BeanList {
            public List<Bean> beans1, beans2;
            public String status1, status2, status3;
        }
        """
      And the following bean class:
        """
        public class BeanListList {
            public List<BeanList> beansList1, beansList2;
            public String status1, status2;
        }
        """

    Scenario: nested list and list consistency
      And operate:
        """
        jFactory.factory(BeanListList.class).spec(ins -> {
            ins.spec().consistent(String.class)
                    .list("beansList1", "beans1").consistent(beansList1Beans1 -> beansList1Beans1
                      .direct("status1"))
                    .list("beansList2", "beans1").consistent(beansList1Beans1 -> beansList1Beans1
                      .direct("status1"));
        });
        """
      When build:
        """
        jFactory.clear().type(BeanListList.class)
                .property("beansList1[0]!.beans1[0]!.status1", "a")
                .property("beansList1[0]!.beans1[1]!", null)
                .property("beansList1[1]!.beans1[0]!", null)
                .property("beansList1[1]!.beans1[1]!.status1", "d")

                .property("beansList2[0]!.beans1[0]!", null)
                .property("beansList2[0]!.beans1[1]!.status1", "b")
                .property("beansList2[1]!.beans1[0]!.status1", "c")
                .property("beansList2[1]!.beans1[1]!", null)
                .create();
        """
      Then the result should:
        """
        : {
          beansList1: [
            {beans1.status1[]= [a b]}
            {beans1.status1[]= [c d]}
          ]
          beansList2: [
            {beans1.status1[]= [a b]}
            {beans1.status1[]= [c d]}
          ]
        }
        """

  Rule: depends on list

    Background:
      And the following bean class:
        """
        public class Beans {
            public List<Bean> beans;
            public int size;
        }
        """

    Scenario: depends on list size
      And the following spec class:
        """
        public class BeansSpec extends Spec<Beans> {
          public void main() {
            property("size").dependsOn("beans", l-> ((List)l).size());
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(BeansSpec.class).property("beans[0]", null).create();
        """
      Then the result should:
        """
        : {
          beans= [null]
          size= 1
        }
        """

#TODO reverseAssociations
#TODO list link

#TODO list index mapping
#TODO nested list index mapping
#TODO different size list consistent
#TODO different size different level list consistent
