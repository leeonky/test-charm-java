Feature: Custom Constructor Toolkit

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

  Rule: Type-Factory Constructor

    Scenario: Instance Context - Access Type Information and Creation Sequence
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

    Scenario: Creation Parameters - Access User-Provided Arguments
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

    Scenario: Value Rotation - Cycle Through Predefined Values
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

    Scenario: Self-Reference Prevention - Cannot Reference Instance Being Created
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

  Rule: Spec-Class Constructor

    Scenario: Instance Context - Access Type Information and Creation Sequence
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          protected Bean construct() {
            return new Bean(instance().type().getSimpleName() + instance().getSequence());
          }
        }
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

    Scenario: Creation Parameters - Access User-Provided Arguments
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          protected Bean construct() {
            return new Bean(instance().param("key"));
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).arg("key", "value").create();
        """
      Then the result should be:
        """
        value= value
        """

    Scenario: Value Rotation - Cycle Through Predefined Values
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          protected Bean construct() {
            return new Bean(instance().rotate("A", "B").get());
          }
        }
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

    Scenario: Self-Reference Prevention - Cannot Reference Instance Being Created
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          protected Bean construct() {
            return new Bean(instance().reference().get());
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        ::throw.message: 'No value'
        """

  Rule: Spec-Factory Constructor

    Background:
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """

    Scenario: Instance Context - Access Type Information and Creation Sequence
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

    Scenario: Creation Parameters - Access User-Provided Arguments
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

    Scenario: Value Rotation - Cycle Through Predefined Values
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

    Scenario: Self-Reference Prevention - Cannot Reference Instance Being Created
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

