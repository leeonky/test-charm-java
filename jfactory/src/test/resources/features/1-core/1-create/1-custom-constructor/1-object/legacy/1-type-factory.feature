Feature: Type-Factory Constructor

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    And the following bean definition:
      """
      public class Bean {
        private Object value;
        public Bean(Object value) { this.value = value; }
        public Object getValue() { return value; }
      }
      """

  Rule: Custom Constructor Applicability by Creation Entry

    Background:
      Given register as follows:
        """
        jFactory.factory(Bean.class).constructor(instance -> new Bean(100));
        """

    Scenario: Global Spec Creation Directly - Invocation via Global Spec Creation Directly
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(GlobalBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.spec(GlobalBeanSpec.class).create();
        """
      Then the result should be:
        """
        value= 100
        """
