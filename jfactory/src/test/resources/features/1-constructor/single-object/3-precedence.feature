Feature: Constructor Resolution Precedence
  Spec-Factory > Spec-Class > Global Spec-Factory > Global Spec > Type Factory > Type Constructor

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

  Scenario: Global Spec Class Constructor > Type Factory Constructor - Global Spec Class Constructor Takes Precedence over Type Factory Constructor
    Given register as follows:
      """
      jFactory.factory(Bean.class).constructor(instance -> new Bean("type"));
      """
    Given the following spec definition:
      """
      @Global
      public class GlobalBeanSpec extends Spec<Bean> {
        @Override
        protected Bean construct() {
          return new Bean("global-spec");
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
      value= global-spec
      """

  Scenario: Global Spec Factory - Global Spec Factory Constructor Takes Precedence over Global Spec Class Constructor
    Given register as follows:
      """
      jFactory.factory(Bean.class).constructor(instance -> new Bean("type"));
      """
    Given the following spec definition:
      """
      @Global
      public class GlobalBeanSpec extends Spec<Bean> {
        @Override
        protected Bean construct() {
          return new Bean("global-spec");
        }
      }
      """
    And register as follows:
      """
      jFactory.specFactory(GlobalBeanSpec.class).constructor(instance -> new Bean("global-spec-factory"));
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
      value= global-spec-factory
      """

  Scenario: Spec-Class Constructor - Spec Class Constructor Takes Precedence over Global Spec Factory Constructor
    Given the following spec definition:
      """
      @Global
      public class GlobalBeanSpec extends Spec<Bean> {
        @Override
        protected Bean construct() {
          return new Bean("global-spec");
        }
      }
      """
    And register as follows:
      """
      jFactory.specFactory(GlobalBeanSpec.class).constructor(instance -> new Bean("global-spec-factory"));
      """
    And the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {
        @Override
        protected Bean construct() {
          return new Bean("spec");
        }
      }
      """
    And register as follows:
      """
      jFactory.factory(Bean.class).constructor(instance -> new Bean("type"));
      """
    When evaluating the following code:
      """
      jFactory.spec(BeanSpec.class).create();
      """
    Then the result should be:
      """
      value= spec
      """

  Scenario: Spec-Factory Constructor - Spec Factory Constructor Takes Precedence over Spec Class Constructor
    Given register as follows:
      """
      jFactory.factory(Bean.class).constructor(instance -> new Bean("type"));
      """
    And the following spec definition:
      """
      @Global
      public class GlobalBeanSpec extends Spec<Bean> {
        protected Bean construct() {
          return new Bean("global-spec");
        }
      }
      """
    And register as follows:
      """
      jFactory.specFactory(GlobalBeanSpec.class).constructor(instance -> new Bean("global-spec-factory"));
      """
    And the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {
        protected Bean construct() {
          return new Bean("spec");
        }
      }
      """
    And register as follows:
      """
      jFactory.specFactory(BeanSpec.class).constructor(instance -> new Bean("spec-factory"));
      """
    When evaluating the following code:
      """
      jFactory.spec(BeanSpec.class).create();
      """
    Then the result should be:
      """
      value= spec-factory
      """

