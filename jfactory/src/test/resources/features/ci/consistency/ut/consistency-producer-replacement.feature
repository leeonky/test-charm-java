Feature: consistency producer replacement

  Background:
    Given declaration jFactory =
    """
    new JFactory();
    """

    Given the following bean class:
      """
      public class Bean {
        public String str1, str2, str3, str4;
      }
      """

  Scenario: should keep original consistency producer
    And the following spec class:
      """
      public class ABean extends Spec<Bean> {
        public void main() {
          consistent(String.class)
            .direct("str1")
            .direct("str2");

          consistent(String.class)
            .direct("str3")
            .property("str2")
              .write(s->s);
        }
      }
      """
    When build:
      """
      jFactory.clear().spec(ABean.class).create();
      """
    Then the result should:
      """
      <<str1,str2>>= /^str1.*/, str3= /^str3.*/
      """
