Feature: Spec Factory Based Custom Constructor

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

  Rule: Define Custom Constructor

    Scenario: Contextual Arguments - Build from Type and Creation Sequence
      Given register as follows:
        """
        jFactory.specFactory(BeanSpec.class).constructor(instance ->
          new Bean(instance.type().getSimpleName() + instance.getSequence()));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        value= Bean1
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        value= Bean2
        """

    Scenario: Parameterized Arguments - Build from Provided Parameters
      Given register as follows:
        """
        jFactory.specFactory(BeanSpec.class).constructor(instance ->
          new Bean(instance.param("key")));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).arg("key", "value").create();
        """
      Then the result should be:
        """
        value= value
        """

    Scenario: Rotating Values - Cycle Through a Predefined List of Values
      Given register as follows:
        """
        jFactory.specFactory(BeanSpec.class).constructor(instance ->
          new Bean(instance.rotate("A", "B").get()));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        value= A
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        value= B
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        value= A
        """

    Scenario: Reference Guard - Prevent Self-Reference in Custom Constructor
      Given register as follows:
        """
        jFactory.specFactory(BeanSpec.class).constructor(instance ->
          new Bean(instance.reference().get()));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        ::throw.message: 'No value'
        """

  Rule: Use Custom Constructor

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

    Scenario: By Spec - Invoke the Constructor via the Spec
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        value= 100
        """

    Scenario: By Spec with Missed Global Spec - Invoke the Constructor via the Spec and a Missed Global Spec
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
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        value= 100
        """

  Rule: Use Custom Constructor in Global Spec Class

    Background:
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(GlobalBeanSpec.class);
        jFactory.specFactory(GlobalBeanSpec.class).constructor(instance -> new Bean(100));
        """

    Scenario: By Type with Global Spec - Invoke the Constructor with the Type and the Global Spec Present
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        value= 100
        """

    Scenario: By Another Missed Spec with Global Spec - Invoke the Constructor with a Missed Spec with the Global Spec Present
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        value= 100
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
