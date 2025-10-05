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


#TODO list index mapping
#TODO nested list index mapping
#TODO different size list consistent
#TODO different size different level list consistent
#TODO consistency on collection not allowed
