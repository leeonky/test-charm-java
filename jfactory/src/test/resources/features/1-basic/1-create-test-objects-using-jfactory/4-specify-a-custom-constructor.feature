Feature: Specify a Custom Constructor

  Background:
    Given the following bean definition:
      """
      public class Bean {
        private int i;
        public Bean(int i) { this.i = i; }
        public int getI() { return i; }
      }
      """
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """

  Scenario: Custom Constructor - Create Bean Use Custom Constructor
    And register as follows:
      """
      jFactory.factory(Bean.class).constructor(arg -> new Bean(100));
      """
    When evaluating the following code:
      """
      jFactory.type(Bean.class).create();
      """
    Then the result should be:
      """
      i= 100
      """
