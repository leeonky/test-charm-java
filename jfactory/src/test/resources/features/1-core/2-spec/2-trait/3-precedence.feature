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

  Rule: Trait vs Trait

    Scenario: Global Spec Class > Type Factory - Global Spec Class Trait Takes Precedence over Type Factory Trait
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

    Scenario: Global Spec Factory > Spec Class - Global Spec Factory Trait Takes Precedence over Global Spec Class Trait
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

    Scenario: Spec Class > Global Spec Factory - Spec Class Trait Takes Precedence over Global Spec Factory Trait
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

    Scenario: Spec Factory > Spec Factory - Spec Factory Trait Takes Precedence over Spec Class Trait
      Given the following spec definition:
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
        jFactory.specFactory(BeanSpec.class).spec("trait", spec -> spec
          .property("value").value("spec-factory"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("trait").create();
        """
      Then the result should be:
        """
        value= spec-factory
        """

    Scenario: Exactly > Regex - An Exact Trait Name Match Takes Precedence Over a Regex Trait
      Given register as follows:
        """
        jFactory.factory(Bean.class)
          .spec("trait", spec -> spec
            .property("value").value("exactly"))
          .spec("trai.*", spec -> spec
            .property("value").value("regex"));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("trait").create();
        """
      Then the result should be:
        """
        value= exactly
        """

  Rule: Trait vs Spec

    Scenario: Trait > Spec - Trait Specification Takes Precedence over Spec Specification
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("value").value("spec");
          }
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec("trait", spec -> spec
            .property("value").value("trait"));
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
