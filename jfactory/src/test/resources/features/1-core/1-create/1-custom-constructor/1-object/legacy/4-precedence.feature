Feature: Constructor Resolution Precedence

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

  Scenario: Spec Factory - Spec Factory Overrides Spec and Type Factory
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

  Scenario: Spec - Spec Construct Overrides Global Spec Factory and Type Factory
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

  Scenario: Global Spec Factory - Global Spec Factory Applies When Spec Has No Construct
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

  Scenario: Global Spec - Global Spec Construct Applies When No Spec Factory Is Defined
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
