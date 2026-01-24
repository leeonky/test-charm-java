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

  Rule: Same Name Trait Precedence

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

    Scenario: Spec-Factory > Spec-Factory - Spec Factory Takes Precedence over Spec Class
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

    Scenario: Whole-Trait Replacement - Same-name Trait Resolution Replaces the Whole Trait Definition, Rather than Merging by Property
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2;
        }
        """
      Given the following spec definition:
        """
        @Global
        public class BeanSpec extends Spec<Bean> {
          @Trait
          public void trait() {
              property("value1").value("trait");
          }
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec("trait", spec -> spec
          .property("value1").value("type-factory-1")
          .property("value2").value("type-factory-2"));
        jFactory.register(BeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("trait").create();
        """
      Then the result should be:
        """
        : {
          value1= trait
          value2= /^value2.*/
        }
        """

  Rule: Trait Spec Precedence

    Scenario: Trait > Spec - Trait Specification Takes Precedence over Spec Specification
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

    Scenario: Replace by Property - When trait and spec both define rules for the same property, only that property’s rules are replaced (other properties remain unchanged)
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2;
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class)
          .spec(spec -> spec
            .property("value1").value("type-factory-1")
            .property("value2").value("type-factory-2"))
          .spec("trait", spec -> spec
            .property("value1").value("type-factory-trait"));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("trait").create();
        """
      Then the result should be:
        """
        : {
          value1= type-factory-trait
          value2= type-factory-2
        }
        """

#TODO error handler missing name missing pattern
