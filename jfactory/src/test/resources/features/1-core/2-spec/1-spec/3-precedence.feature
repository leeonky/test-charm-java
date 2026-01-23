Feature: Spec Override Strategy

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    And the following bean definition:
      """
      public class Bean {
        public String value;
      }
      """

  Scenario: Global Spec Class > Type Factory - Global Spec Class Takes Precedence over Type Factory Specs
    Given the following spec definition:
      """
      @Global
      public class GlobalBeanSpec extends Spec<Bean> {
        public void main() {
            property("value").value("global-spec-class");
        }
      }
      """
    And register as follows:
      """
      jFactory.factory(Bean.class).spec(spec -> spec
        .property("value").value("type-factory"));
      jFactory.register(GlobalBeanSpec.class);
      """
    When evaluating the following code:
      """
      jFactory.create(Bean.class);
      """
    Then the result should be:
      """
      value= global-spec-class
      """

  Scenario: Global Spec Factory > Spec Class - Global Spec Factory Takes Precedence over Global Spec Class
    Given the following spec definition:
      """
      @Global
      public class GlobalBeanSpec extends Spec<Bean> {
        public void main() {
            property("value").value("global-spec-class");
        }
      }
      """
    And register as follows:
      """
      jFactory.specFactory(GlobalBeanSpec.class).spec(spec -> spec
        .property("value").value("global-spec-factory"));
      """
    When evaluating the following code:
      """
      jFactory.create(Bean.class);
      """
    Then the result should be:
      """
      value= global-spec-factory
      """

  Scenario: Spec Class > Global Spec Factory - Spec Class Takes Precedence over Global Spec Factory
    Given the following spec definition:
      """
      @Global
      public class GlobalBeanSpec extends Spec<Bean> {}
      """
    And the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {
        public void main() {
          property("value").value("spec-class");
        }
      }
      """
    And register as follows:
      """
      jFactory.specFactory(GlobalBeanSpec.class).spec(spec -> spec
        .property("value").value("global-spec-factory"));
      """
    When evaluating the following code:
      """
      jFactory.createAs(BeanSpec.class);
      """
    Then the result should be:
      """
      value= spec-class
      """

  Scenario: Spec Factory > Spec Factory - Spec Factory Takes Precedence over Spec Class
    Given the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {
        public void main() {
          property("value").value("spec-class");
        }
      }
      """
    And register as follows:
      """
      jFactory.specFactory(BeanSpec.class).spec(spec -> spec
        .property("value").value("spec-factory"));
      """
    When evaluating the following code:
      """
      jFactory.createAs(BeanSpec.class);
      """
    Then the result should be:
      """
      value= spec-factory
      """

