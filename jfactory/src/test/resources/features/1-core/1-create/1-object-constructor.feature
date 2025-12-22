Feature: Object Constructor

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """

  Scenario: Default Object Constructor - Create an Object by Default Class Constructor with All Default Values
    Given the following bean definition:
      """
      public class Bean {
        public String stringValue;
        public int intValue;
      }
      """
    When evaluating the following code:
      """
      jFactory.create(Bean.class);
      """
    Then the result should be:
      """
      : {
        stringValue= stringValue#1
        intValue= 1
      }
      """

  Scenario: Custom Constructor - Create Bean Use Custom Constructor by Type
    Given the following bean definition:
      """
      public class Bean {
        private int i;
        public Bean(int i) { this.i = i; }
        public int getI() { return i; }
      }
      """
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

  Scenario: Custom Constructor - Create Bean Use Custom Constructor by Spec
    Given the following bean definition:
      """
      public class Bean {
        private int i;
        public Bean(int i) { this.i = i; }
        public int getI() { return i; }
      }
      """
    And the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {}
      """
    And register as follows:
      """
      jFactory.factory(Bean.class).constructor(arg -> new Bean(100));
      """
    When evaluating the following code:
      """
      jFactory.spec(BeanSpec.class).create();
      """
    Then the result should be:
      """
      i= 100
      """

  Scenario: Define in Spec Lambda - Define Custom Constructor in Spec Lambda
    Given the following bean definition:
      """
      public class Bean {
        private int i;
        public Bean(int i) { this.i = i; }
        public int getI() { return i; }
      }
      """
    And the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {}
      """
    And register as follows:
      """
      jFactory.specFactory(BeanSpec.class).constructor(arg -> new Bean(100));
      """
    When evaluating the following code:
      """
      jFactory.spec(BeanSpec.class).create();
      """
    Then the result should be:
      """
      i= 100
      """

  Scenario: Define in Spec Class - Define Custom Constructor in Spec Class
    Given the following bean definition:
      """
      public class Bean {
        private int i;
        public Bean(int i) { this.i = i; }
        public int getI() { return i; }
      }
      """
    And the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {
        protected Bean construct() {
          return new Bean(100);
        }
      }
      """
    When evaluating the following code:
      """
      jFactory.spec(BeanSpec.class).create();
      """
    Then the result should be:
      """
      i= 100
      """

  Scenario: Default List Constructor - Create a Empty List by Default List Constructor with All Default Values
    When evaluating the following code:
      """
      jFactory.create(String[].class);
      """
    Then the result should be:
      """
      : {
        class.simpleName= 'String[]',
        ::this= []
      }
      """

  Scenario: Construct List with Element - Create a List by Default List Constructor with Element Size
    When evaluating the following code:
      """
      jFactory.type(String[].class).property("[0]", "hello").create();
      """
    Then the result should be:
      """
      : {
        class.simpleName= 'String[]',
        ::this= [hello]
      }
      """

  Scenario: Custom Constructor of List - Create a List by Custom List Constructor
    And register as follows:
      """
      jFactory.factory(String[].class).constructor(arg -> new String[] {"custom"});
      """
    When evaluating the following code:
      """
      jFactory.type(String[].class).create();
      """
    Then the result should be:
      """
      : {
        class.simpleName= 'String[]',
        ::this= [custom]
      }
      """
