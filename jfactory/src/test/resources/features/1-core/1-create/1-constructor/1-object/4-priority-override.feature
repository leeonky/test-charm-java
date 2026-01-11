Feature: Priority and Override

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    And the following bean definition:
      """
      public class Bean {
        private String value;
        public Bean(String value) { this.value = value; }
        public String getValue() { return value; }
      }
      """

  Scenario: Spec Override Type
    Given register as follows:
      """
      jFactory.register(Bean.
      """

#    type / spec / spec factory / global spec / global spec factory