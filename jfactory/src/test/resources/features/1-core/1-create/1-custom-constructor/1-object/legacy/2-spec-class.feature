Feature: Spec-Class Constructor

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
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          protected Bean construct() {
            return new Bean(100);
          }
        }
        """
      And register as follows:
        """
        jFactory.register(BeanSpec.class);
        """

    Scenario: Type Creation - Does Not Invoke the Constructor
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        ::throw.class.simpleName= NoAppropriateConstructorException
        """

    Scenario: Non-matching Spec Creation - Does Not Invoke the Constructor
      Given the following spec definition:
        """
        public class NonMatchingBeanSpec extends Spec<Bean> {}
        """
      When evaluating the following code:
        """
        jFactory.spec(NonMatchingBeanSpec.class).create();
        """
      Then the result should be:
        """
        ::throw.class.simpleName= NoAppropriateConstructorException
        """

    Scenario: Type Creation with Non-matching Global Spec - Does Not Invoke the Constructor
      Given the following spec definition:
        """
        @Global
        public class MissedGlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(MissedGlobalBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        ::throw.class.simpleName= NoAppropriateConstructorException
        """

    Scenario: Non-matching Spec Creation with Non-matching Global Spec - Does Not Invoke the Constructor
      Given the following spec definition:
        """
        @Global
        public class MissedGlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(MissedGlobalBeanSpec.class);
        """
      Given the following spec definition:
        """
        public class NonMatchingBeanSpec extends Spec<Bean> {}
        """
      When evaluating the following code:
        """
        jFactory.spec(NonMatchingBeanSpec.class).create();
        """
      Then the result should be:
        """
        ::throw.class.simpleName= NoAppropriateConstructorException
        """

    Scenario: Non-matching Global Spec Creation Directly - Does Not Invoke the Constructor
      Given the following spec definition:
        """
        @Global
        public class MissedGlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(MissedGlobalBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.spec(MissedGlobalBeanSpec.class).create();
        """
      Then the result should be:
        """
        ::throw.class.simpleName= NoAppropriateConstructorException
        """

  Rule: Custom Constructor Applicability by Creation Entry with Global Spec

    Background:
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          protected Bean construct() {
            return new Bean(100);
          }
        }
        """
      And register as follows:
        """
        jFactory.register(GlobalBeanSpec.class);
        """

    Scenario: By Global Spec Directly - Invoke Custom Constructor with the Global Spec Directly
      When evaluating the following code:
        """
        jFactory.spec(GlobalBeanSpec.class).create();
        """
      Then the result should be:
        """
        value= 100
        """
