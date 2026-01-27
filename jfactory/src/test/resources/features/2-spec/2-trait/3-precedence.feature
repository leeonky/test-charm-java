Feature: Trait Override Strategy

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

  Rule: Whole Trait Precedence

    Scenario: Global Spec-Class > Type-Factory - Global Spec Class Takes Precedence over Type Factory
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          @Trait
          public void trait() {
              property("value").value("global-spec-class");
          }
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec("trait", spec -> spec
          .property("value").value("type-factory"));
        jFactory.register(GlobalBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("trait").create();
        """
      Then the result should be:
        """
        value= global-spec-class
        """

    Scenario: Global Spec-Factory > Spec-Class - Global Spec Factory Takes Precedence over Global Spec Class
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          @Trait
          public void trait() {
              property("value").value("global-spec-class");
          }
        }
        """
      And register as follows:
        """
        jFactory.specFactory(GlobalBeanSpec.class).spec("trait", spec -> spec
          .property("value").value("global-spec-factory"));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("trait").create();
        """
      Then the result should be:
        """
        value= global-spec-factory
        """

    Scenario: Spec-Class > Global Spec-Factory - Spec Class Takes Precedence over Global Spec Factory
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {}
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          @Trait
          public void trait() {
            property("value").value("spec-class");
          }
        }
        """
      And register as follows:
        """
        jFactory.specFactory(GlobalBeanSpec.class).spec("trait", spec -> spec
          .property("value").value("global-spec-factory"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("trait").create();
        """
      Then the result should be:
        """
        value= spec-class
        """

  Rule: Property-Level Spec Precedence

    Scenario: Type-Factory Trait > Spec-Factory Spec - Trait Specification Takes Precedence over Spec Specification
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec("trait", spec -> spec
            .property("value").value("trait"));
        jFactory.specFactory(BeanSpec.class).spec(spec -> spec
            .property("value").value("spec"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("trait").create();
        """
      Then the result should be:
        """
        value= trait
        """

#TODO error handler missing name missing pattern
