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

#  Rule: two dimensional list
#
#    Background:
#      Given the following bean class:
#        """
#        public class Beans {
#          public Bean beans[]
#        }
#        """
#      Given the following bean class:
#        """
#        public class BeanLists {
#          public Beans beansList1[], beansList2[];
#        }
#        """
#      And register:
#        """
#        jFactory.factory(Beans.class).spec(ins -> {
#          ins.spec().structure()
#            .list("beans1")
#            .list("beans2");
#        });
#        """

# TODO multi dimensional list
# TODO custom normalizer for index mapping
# TODO custom normalized coordinate type
# TODO populate by spec
# TODO reverseAssociation
