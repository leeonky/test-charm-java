Feature: Type Based Custom Constructor

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

  Rule: Define Custom Constructor

    Scenario: Contextual Arguments - Build from Type and Creation Sequence
      And register as follows:
        """
        jFactory.factory(Bean.class).constructor(instance -> new Bean(instance.type().getSimpleName() + instance.getSequence()));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        value= Bean1
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        value= Bean2
        """

    Scenario: Parameterized Arguments - Build from Provided Parameters
      Given register as follows:
        """
        jFactory.factory(Bean.class).constructor(instance -> new Bean(instance.param("key")));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).arg("key", "value").create();
        """
      Then the result should be:
        """
        value= value
        """

    Scenario: Rotating Values - Cycle Through a Predefined List of Values
      Given register as follows:
        """
        jFactory.factory(Bean.class).constructor(instance -> new Bean(instance.rotate("A", "B").get()));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        value= A
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        value= B
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        value= A
        """

    Scenario: Reference Guard - Prevent Self-Reference in Custom Constructor
      Given register as follows:
        """
        jFactory.factory(Bean.class).constructor(instance -> new Bean(instance.reference().get()));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        ::throw.message: 'No value'
        """

  Rule: Use Custom Constructor

    Background:
      Given register as follows:
        """
        jFactory.factory(Bean.class).constructor(instance -> new Bean(100));
        """

    Scenario: By Type - Invoke Custom Constructor via Type
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        value= 100
        """

    Scenario: By Spec - Invoke Custom Constructor via Spec
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

    Scenario: By Type with Global Spec - Invoke Custom Constructor via Type with Global Spec
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
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        value= 100
        """

    Scenario: By Spec with Global Spec - Invoke Custom Constructor via Spec with Global Spec
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(BeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        value= 100
        """

    Scenario: By Global Spec Directly - Invoke Custom Constructor via Global Spec Directly
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
