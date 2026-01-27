Feature: Custom Constructor Registration in Different Object Creation Forms

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

  Rule: Type Creation

    Scenario: Type-Factory Constructor - Define Custom Constructor in Type Factory
      Given register as follows:
        """
        jFactory.factory(Bean.class).constructor(instance -> new Bean(100));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        value= 100
        """

    Scenario: Non-Global Spec-Class Constructor - Registration Has No Effect on Type Creation!
      Given the following spec definition:
        """
        public class AnyBeanSpec extends Spec<Bean> {
          protected Bean construct() {
              return new Bean(100);
          }
        }
        """
      And register as follows:
        """
        jFactory.register(AnyBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        ::throw.class.simpleName= NoAppropriateConstructorException
        """

    Scenario: Non-Global Spec-Factory Constructor - Registration Has No Effect on Type Creation!
      Given the following spec definition:
        """
        public class AnyBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.specFactory(AnyBeanSpec.class).constructor(instance -> new Bean(100));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        ::throw.class.simpleName= NoAppropriateConstructorException
        """

  Rule: Type Creation with Global Spec

    Scenario: Global Spec-Class Constructor - Define Custom Constructor in Global Spec Class
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
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        value= 100
        """

    Scenario: Global Spec-Factory Constructor - Define Custom Constructor in Global Spec Factory
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.specFactory(GlobalBeanSpec.class).constructor(instance -> new Bean(100));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        value= 100
        """

    Scenario: Non-Constructing Global Spec - Will Use Type-Factory Constructor
      Given register as follows:
        """
        jFactory.factory(Bean.class).constructor(instance -> new Bean(100));
        """
      And the following spec definition:
        """
        @Global
        public class NonConstructingGlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(NonConstructingGlobalBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        value= 100
        """

  Rule: Spec Creation

    Scenario: Type-Factory Constructor - Define Custom Constructor in Type Factory
      Given register as follows:
        """
        jFactory.factory(Bean.class).constructor(instance -> new Bean(100));
        """
      And the following spec definition:
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

    Scenario: Spec-Class Constructor - Define Custom Constructor in Spec Class
      Given the following spec definition:
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
        value= 100
        """

    Scenario: Spec-Factory Constructor - Define Custom Constructor in Spec Factory
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.specFactory(BeanSpec.class).constructor(instance -> new Bean(100));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        value= 100
        """

    Scenario: Non-Matching Spec-Class Constructor - Registration Has No Effect on Spec Creation!
      Given the following spec definition:
        """
        public class NonMatchingBeanSpec extends Spec<Bean> {
          protected Bean construct() {
              return new Bean(100);
          }
        }
        """
      And register as follows:
        """
        jFactory.register(NonMatchingBeanSpec.class);
        """
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
        ::throw.class.simpleName= NoAppropriateConstructorException
        """

    Scenario: Non-Matching Spec-Factory Constructor - Registration Has No Effect on Spec Creation!
      Given the following spec definition:
        """
        public class NonMatchingBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.specFactory(NonMatchingBeanSpec.class).constructor(instance -> new Bean(100));
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        ::throw.class.simpleName= NoAppropriateConstructorException
        """

  Rule: Spec Creation with Global Spec

    Scenario: Global Spec-Class Constructor - Define Custom Constructor in Global Spec Class
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
      And the following spec definition:
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

    Scenario: Global Spec-Factory Constructor - Define Custom Constructor in Global Spec Factory
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.specFactory(GlobalBeanSpec.class).constructor(instance -> new Bean(100));
        """
      And the following spec definition:
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

    Scenario: Non-Constructing Global Spec - Will Use Spec Class Constructor
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          protected Bean construct() {
            return new Bean(100);
          }
        }
        """
      And the following spec definition:
        """
        @Global
        public class NonConstructingGlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(NonConstructingGlobalBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        value= 100
        """

    Scenario: Non-Constructing Global Spec - Will Use Spec Factory Constructor
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.specFactory(BeanSpec.class).constructor(instance -> new Bean(100));
        """
      And the following spec definition:
        """
        @Global
        public class NonConstructingGlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(NonConstructingGlobalBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        value= 100
        """

    Scenario: Non-Constructing Global Spec - Will Use Type Factory Constructor
      Given register as follows:
        """
        jFactory.factory(Bean.class).constructor(instance -> new Bean(100));
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      And the following spec definition:
        """
        @Global
        public class NonConstructingGlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(NonConstructingGlobalBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        value= 100
        """

    Scenario: Non-Matching Spec-Class Constructor with Non-Constructing Global Spec - Registration Has No Effect on Spec Creation!
      Given the following spec definition:
        """
        public class NonMatchingBeanSpec extends Spec<Bean> {
          protected Bean construct() {
              return new Bean(100);
          }
        }
        """
      And register as follows:
        """
        jFactory.register(NonMatchingBeanSpec.class);
        """
      And the following spec definition:
        """
        @Global
        public class NonConstructingGlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(NonConstructingGlobalBeanSpec.class);
        """
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
        ::throw.class.simpleName= NoAppropriateConstructorException
        """

    Scenario: Non-Matching Spec-Factory Constructor with Non-Constructing Global Spec - Registration Has No Effect on Spec Creation!
      Given the following spec definition:
        """
        public class NonMatchingBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.specFactory(NonMatchingBeanSpec.class).constructor(instance -> new Bean(100));
        """
      And the following spec definition:
        """
        @Global
        public class NonConstructingGlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(NonConstructingGlobalBeanSpec.class);
        """
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
        ::throw.class.simpleName= NoAppropriateConstructorException
        """

  Rule: Directly Global Spec Creation

    Scenario: Non-Constructing Global Spec - Will Use Type Factory Constructor
      Given register as follows:
        """
        jFactory.factory(Bean.class).constructor(instance -> new Bean(100));
        """
      And the following spec definition:
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

    Scenario: Global Spec-Class Constructor - Define Custom Constructor in Global Spec Class
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
      When evaluating the following code:
        """
        jFactory.spec(GlobalBeanSpec.class).create();
        """
      Then the result should be:
        """
        value= 100
        """

    Scenario: Global Spec-Factory Constructor - Define Custom Constructor in Global Spec Factory
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.specFactory(GlobalBeanSpec.class).constructor(instance -> new Bean(100));
        """
      When evaluating the following code:
        """
        jFactory.spec(GlobalBeanSpec.class).create();
        """
      Then the result should be:
        """
        value= 100
        """

    Scenario: Non-Constructing Global Spec with Non-Matching Spec-Class Constructor - Registration Has No Effect on Spec Creation!
      Given the following spec definition:
        """
        @Global
        public class NonConstructingGlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(NonConstructingGlobalBeanSpec.class);
        """
      And the following spec definition:
        """
        public class NonMatchingBeanSpec extends Spec<Bean> {
          protected Bean construct() {
              return new Bean(100);
          }
        }
        """
      And register as follows:
        """
        jFactory.register(NonMatchingBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.spec(NonConstructingGlobalBeanSpec.class).create();
        """
      Then the result should be:
        """
        ::throw.class.simpleName= NoAppropriateConstructorException
        """

    Scenario: Non-Constructing Global Spec with Non-Matching Spec-Factory Constructor - Registration Has No Effect on Spec Creation!
      Given the following spec definition:
        """
        @Global
        public class NonConstructingGlobalBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(NonConstructingGlobalBeanSpec.class);
        """
      And the following spec definition:
        """
        public class NonMatchingBeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.specFactory(NonMatchingBeanSpec.class).constructor(instance -> new Bean(100));
        """
      When evaluating the following code:
        """
        jFactory.spec(NonConstructingGlobalBeanSpec.class).create();
        """
      Then the result should be:
        """
        ::throw.class.simpleName= NoAppropriateConstructorException
        """

