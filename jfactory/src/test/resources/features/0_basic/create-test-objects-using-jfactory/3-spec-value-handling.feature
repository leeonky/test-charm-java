Feature: Spec Value Handling

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """

  Scenario: Spec Value - Specify Spec Value in Spec
    Given the following bean definition:
      """
      public class Bean {
        public String str;
      }
      """
    When register as follows:
      """
      jFactory.factory(Bean.class).spec(ins -> ins.spec().property("str").value("hello"));
      """
    And evaluating the following code:
      """
      jFactory.type(Bean.class).create();
      """
    Then the result should be:
      """
      str= hello
      """

  Scenario: Lambda Spec Value - Use a Lambda for Spec Value
    Given the following bean definition:
      """
      public class Bean {
        public String str;
      }
      """
    When register as follows:
      """
      jFactory.factory(Bean.class).spec(ins -> ins.spec().property("str").value(() -> "from_lambda"));
      """
    And evaluating the following code:
      """
      jFactory.type(Bean.class).create();
      """
    Then the result should be:
      """
      str= from_lambda
      """

  Scenario: Value Override - Prioritize Input Property Values Over Spec Value
    Given the following bean definition:
      """
      public class Bean {
        public String str;
      }
      """
    And register as follows:
      """
      jFactory.factory(Bean.class).spec(ins -> ins.spec()
        .property("str").value("spec-value")
      );
      """
    When evaluating the following code:
      """
      jFactory.type(Bean.class)
        .property("str", "input-value")
        .create()
      """
    Then the result should be:
      """
      str= input-value
      """
