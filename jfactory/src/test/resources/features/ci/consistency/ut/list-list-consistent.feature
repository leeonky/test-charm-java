Feature: list - list consistency

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

  Rule: default index coordinate mapping

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

  Rule: custom index and coordinate mapping

    Background:
      And the following bean class:
        """
        public class BeanList {
            public List<Bean> beans1, beans2;
            public String status1, status2, status3;
        }
        """

    Scenario: reverse mapping
      And operate:
        """
        jFactory.factory(BeanList.class).spec(ins -> {
            ins.spec().consistent(String.class)
                .list("beans1").normalize(Normalizer.reverse()).consistent(beans1 -> beans1
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
                .property("beans2[0]!.status1", "b")
                .property("beans2[1]!", "")
                .create();
        """
      Then the result should:
        """
        :      | status1[] |
        beans1 | [a b]     |
        beans2 | [b a]     |
        """

    Scenario: reverse mapping through separated api
      And operate:
        """
        jFactory.factory(BeanList.class).spec(ins -> {
            ins.spec().consistent(String.class, Coordinate.D1.class)
                .list("beans1").normalize(d1 -> Coordinate.d1(d1.index().reverse()),
                                          d1 -> Coordinate.d1(d1.index().reverse())).consistent(beans1 -> beans1
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
                .property("beans2[0]!.status1", "b")
                .property("beans2[1]!", "")
                .create();
        """
      Then the result should:
        """
        :      | status1[] |
        beans1 | [a b]     |
        beans2 | [b a]     |
        """

    Scenario: nested list and list consistency
      And the following bean class:
        """
        public class BeanListList {
            public List<BeanList> beansList1, beansList2;
            public String status1, status2;
        }
        """
      And operate:
        """
        jFactory.factory(BeanListList.class).spec(ins -> {
            ins.spec().consistent(String.class)
                    .list("beansList1", "beans1").normalize(Normalizer.reverse()).consistent(beansList1Beans1 -> beansList1Beans1
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
            {beans1.status1[]= [a c]}
            {beans1.status1[]= [b d]}
          ]
          beansList2: [
            {beans1.status1[]= [d b]}
            {beans1.status1[]= [c a]}
          ]
        }
        """

    Scenario: different size list consistent
      And operate:
        """
        jFactory.factory(BeanList.class).spec(ins -> {
            ins.spec().property("beans1[]").byFactory();
            ins.spec().property("beans2[]").byFactory();

            ins.spec().consistent(String.class)
                    .list("beans1").normalize(Normalizer.sample(2, 0)).consistent(beans1 -> beans1
                      .direct("status1"))
                    .list("beans2").consistent(beans2 -> beans2
                      .direct("status1"));

            ins.spec().consistent(String.class)
                    .list("beans1").normalize(Normalizer.sample(2, 1)).consistent(beans1 -> beans1
                      .direct("status1"))
                    .list("beans2").consistent(beans2 -> beans2
                      .direct("status2"));
        });
        """
      When build:
        """
        jFactory.clear().type(BeanList.class)
          .property("beans1[0]!.status1", "a")
          .property("beans1[1]!.status1", "b")
          .property("beans1[2]!.status1", "c")
          .property("beans1[3]!.status1", "d")
          .create();
        """
      Then the result should:
        """
        : {
          beans1.status1[]= [a b c d]
          beans2: | status1 | status2|
                  | a       | b      |
                  | c       | d      |
        }
        """
      When build:
        """
        jFactory.clear().type(BeanList.class)
          .property("beans2[0]!.status1", "a")
          .property("beans2[0]!.status2", "b")
          .property("beans2[1]!.status1", "c")
          .property("beans2[1]!.status2", "d")
          .create();
        """
      Then the result should:
        """
        : {
          beans1.status1[]= [a b c d]
          beans2: | status1 | status2|
                  | a       | b      |
                  | c       | d      |
        }
        """

    Scenario: swap list index
      And the following bean class:
        """
        public class BeanListList {
            public List<BeanList> beansList1, beansList2;
            public String status1, status2;
        }
        """
      And operate:
        """
        jFactory.factory(BeanList.class).spec(ins -> {
            ins.spec().property("beans1[]").byFactory();
        });
        """
      And operate:
        """
        jFactory.factory(BeanListList.class).spec(ins -> {
            ins.spec().property("beansList1[]").byFactory();
            ins.spec().property("beansList2[]").byFactory();

            ins.spec().consistent(String.class)
                    .list("beansList1", "beans1").normalize(Normalizer.transpose()).consistent(beansList1Beans1 -> beansList1Beans1
                      .direct("status1"))
                    .list("beansList2", "beans1").consistent(beansList1Beans1 -> beansList1Beans1
                      .direct("status1"));
        });
        """
      When build:
        """
        jFactory.clear().type(BeanListList.class)
          .property("beansList1[0]!.beans1[0]!.status1", "a")
          .property("beansList1[0]!.beans1[1]!.status1", "b")
          .property("beansList1[0]!.beans1[2]!.status1", "c")

          .property("beansList1[1]!.beans1[0]!.status1", "e")
          .property("beansList1[1]!.beans1[1]!.status1", "f")
          .property("beansList1[1]!.beans1[2]!.status1", "g")
          .create();
        """
      Then the result should:
        """
        : {
          beansList1: | beans1.status1[] |
                      | [a b c ]         |
                      | [e f g ]         |

          beansList2: | beans1.status1[] |
                      | [a e ]           |
                      | [b f ]           |
                      | [c g ]           |
        }
        """

#TODO consistency on collection not allowed
