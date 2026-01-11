Feature: Spec-Factory Constructor

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
    And the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {}
      """

  Rule: Custom Constructor Applicability by Creation Entry

    Background:
      Given register as follows:
        """
        jFactory.specFactory(BeanSpec.class).constructor(instance -> new Bean(100));
        """

    Scenario: By Type - Does Not Invoke the Constructor
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        ::throw.class.simpleName= NoAppropriateConstructorException
        """

    Scenario: By Missed Spec - Does Not Invoke the Constructor
      Given the following spec definition:
        """
        public class MissedBeanSpec extends Spec<Bean> {}
        """
      When evaluating the following code:
        """
        jFactory.spec(MissedBeanSpec.class).create();
        """
      Then the result should be:
        """
        ::throw.class.simpleName= NoAppropriateConstructorException
        """

    Scenario: By Type with Missed Global Spec - Does Not Invoke the Constructor
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

    Scenario: By Missed Spec with Missed Global Spec - Does Not Invoke the Constructor
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
        public class MissedBeanSpec extends Spec<Bean> {}
        """
      When evaluating the following code:
        """
        jFactory.spec(MissedBeanSpec.class).create();
        """
      Then the result should be:
        """
        ::throw.class.simpleName= NoAppropriateConstructorException
        """

    Scenario: By Missed Global Spec Directly - Does Not Invoke the Constructor
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
        public class GlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.specFactory(GlobalBeanSpec.class).constructor(instance -> new Bean(100));
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
