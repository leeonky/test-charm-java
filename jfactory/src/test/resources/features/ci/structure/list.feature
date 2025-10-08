Feature: populate list depends on another list

  Background:
    Given declaration jFactory =
      """
      new JFactory();
      """
    Given the following bean class:
      """
      public class Bean {
        public String str;
      }
      """

  Rule: one dimensional list

    Background:
      Given the following bean class:
      """
      public class Beans {
        public Bean beans1[], beans2[];
      }
      """

    Scenario: list population depends on another list null default value element
      And register:
        """
        jFactory.factory(Beans.class).spec(ins -> {
          ins.spec().structure()
            .list("beans1")
            .list("beans2");
        });
        """
      When build:
        """
        jFactory.clear().type(Beans.class).property("beans1[1].str", "world").create();
        """
      Then the result should:
        """
        : {
          beans1: [null {str= world}]
          beans2: [null {...}]
        }
        """

    Scenario: list population depends on each other
      And register:
        """
        jFactory.factory(Beans.class).spec(ins -> {
          ins.spec().structure()
            .list("beans1")
            .list("beans2");
        });
        """
      When build:
        """
        jFactory.clear().type(Beans.class).property("beans1[1].str", "world").property("beans2[0].str", "hello").create();
        """
      Then the result should:
        """
        : {
          beans1: [{...} {str= world}]
          beans2: [{str= hello} {...}]
        }
        """

    Scenario: should not change element when already has value
      And register:
        """
        jFactory.factory(Beans.class).spec(ins -> {
          ins.spec().structure()
            .list("beans1")
            .list("beans2");
        });
        """
      When build:
        """
        jFactory.clear().type(Beans.class).property("beans1[0].str", "hello").property("beans1[1].str", "world")
        .property("beans2[0].str", "any").create();
        """
      Then the result should:
        """
        : {
          beans1: [{str= hello} {str= world}]
          beans2: [{str= any} {...}]
        }
        """

    Scenario: custom normalizer
      And register:
        """
        jFactory.factory(Beans.class).spec(ins -> {
          ins.spec().structure()
            .list("beans1").normalize(Normalizer.sample(2, 0))
            .list("beans2");
          ins.spec().structure()
            .list("beans1").normalize(Normalizer.sample(2, 1))
            .list("beans2");
        });
        """
      When build:
        """
        jFactory.clear().type(Beans.class)
          .property("beans1[0].str", "hello")
          .property("beans1[1].str", "world")
          .create();
        """
      Then the result should:
        """
        : {
            beans1: [{str= hello} {str= world}]
            beans2: [{...}]
        }
        """
      When build:
        """
        jFactory.clear().type(Beans.class)
          .property("beans2[0].str", "hello")
          .create();
        """
      Then the result should:
        """
        : {
            beans1: [{...} {...}]
            beans2: [{str= hello}]
        }
        """

  Rule: two dimensional list

    Background:
      Given the following bean class:
        """
        public class Beans {
          public Bean beans[];
        }
        """
      Given the following bean class:
        """
        public class BeanLists {
          public Beans beansList1[], beansList2[];
        }
        """

    Scenario: two dimensional list population
      And register:
        """
        jFactory.factory(BeanLists.class).spec(ins -> {
          ins.spec().structure()
            .list("beansList1", "beans")
            .list("beansList2", "beans");
        });
        """
      When build:
        """
        jFactory.clear().type(BeanLists.class)
          .property("beansList1[0].beans[0].str", "a")
          .property("beansList1[0].beans[1].str", "b")
          .property("beansList1[1].beans[0].str", "c")
          .property("beansList1[1].beans[1].str", "d").create();
        """
      Then the result should:
        """
        : {
          beansList1: | beans.str[] |
                      | [a b]       |
                      | [c d]       |

          beansList2: | beans         |
                      | [{...} {...}] |
                      | [{...} {...}] |
        }
        """

    Scenario: custom normalizer
      And register:
        """
        jFactory.factory(BeanLists.class).spec(ins -> {
          ins.spec().structure(Coordinate.D2.class)
            .list("beansList1", "beans").normalize(d2->Coordinate.d2(d2.index1(), d2.index0()),
              d2->Coordinate.d2(d2.index1(), d2.index0()))
            .list("beansList2", "beans");
        });
        """
      When build:
        """
        jFactory.clear().type(BeanLists.class)
          .property("beansList1[0].beans[0].str", "a")
          .property("beansList1[0].beans[1].str", "b")
          .property("beansList1[0].beans[2].str", "c")
          .property("beansList1[1].beans[0].str", "d")
          .property("beansList1[1].beans[1].str", "e")
          .property("beansList1[1].beans[2].str", "f")
          .create();
        """
      Then the result should:
        """
        : {
          beansList1: | beans.str[] |
                      | [a b c]     |
                      | [d e f]     |

          beansList2: | beans         |
                      | [{...} {...}] |
                      | [{...} {...}] |
                      | [{...} {...}] |
        }
        """

# TODO multi dimensional list
# TODO populate by spec
# TODO reverseAssociation
