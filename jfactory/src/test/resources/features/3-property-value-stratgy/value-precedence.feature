Feature: Value Precedence

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """

  Rule: TODO

#      TODO move to another feature
    Scenario: Value Override - Prioritize Input Property Values Over Any Defaults
      Given the following bean definition:
        """
        public class Bean {
          public String str1, str2, str3;
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec(spec -> spec
          .property("str1").ignore()
          .property("str2").defaultValue("any")
        );
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("str1", "hello")
          .property("str2", "world")
          .property("str3", "!")
          .create();
        """
      Then the result should be:
        """
        : {str1= hello, str2= world, str3= '!'}
        """
