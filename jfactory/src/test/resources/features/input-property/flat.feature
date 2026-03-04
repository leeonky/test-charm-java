Feature: flat property

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """

  Rule: Object

    Background:
      Given the following class definition:
        """
        public class Bean {
          public String value1, value2;
        }
        """

    Scenario: Create by Single Property and Value
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("value1", "v1")
          .property("value2", "v2")
          .create();
        """
      Then the result should be:
        """
        : {
          value1= v1
          value2= v2
        }
        """

    Scenario: The Later Input Value Replaces the Previous One
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("value1", "v1")
          .property("value1", "v2")
          .create();
        """
      Then the result should be:
        """
        value1= v2
        """

#  Rule: Collection
#    Background:
