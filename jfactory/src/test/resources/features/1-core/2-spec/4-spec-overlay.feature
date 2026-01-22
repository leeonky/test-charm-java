Feature: Spec / Trait Overlay Strategy

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """

  Scenario: Trait Overlay - Trait Overlay from Spec Factory / Spec Class / Global Spec Factory / Global Spec Class / Type Factory
    Given the following bean definition:
      """
      public class Bean {
        public String value1, value2, value3, value4, value5;
      }
      """
    And register as follows:
      """
      jFactory.factory(Bean.class).spec("TypeFactory", spec -> spec
        .property("value5").value("type-factory"));
      """
    And the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {
        @Trait
        public void SpecClass() {
            property("value2").value("spec-class");
        }
      }
      """
    And the following spec definition:
      """
      @Global
      public class GlobalBeanSpec extends Spec<Bean> {
        @Trait
        public void GlobalSpecClass() {
            property("value4").value("global-spec-class");
        }
      }
      """
    And register as follows:
      """
      jFactory.specFactory(BeanSpec.class).spec("SpecFactory", spec -> spec
        .property("value1").value("spec-factory"));
      jFactory.specFactory(GlobalBeanSpec.class).spec("GlobalSpecFactory", spec -> spec
        .property("value3").value("global-spec-factory"));
      """
    When evaluating the following code:
      """
      jFactory.spec(BeanSpec.class).traits("SpecFactory", "SpecClass", "GlobalSpecFactory", "GlobalSpecClass", "TypeFactory").create();
      """
    Then the result should be:
      """
      : {
        value1= spec-factory
        value2= spec-class
        value3= global-spec-factory
        value4= global-spec-class
        value5= type-factory
      }
      """

  Rule: Spec Precedence

    Background:
      Given the following bean definition:
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

  Rule: Trait Precedence

    Background:
      Given the following bean definition:
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

    Scenario: Global Spec Factory > Spec Class - Global Spec Factory Takes Precedence over Global Spec Class
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

    Scenario: Spec Class > Global Spec Factory - Spec Class Takes Precedence over Global Spec Factory
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

    Scenario: Spec Factory > Spec Factory - Spec Factory Takes Precedence over Spec Class
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

#TODO error handler missing name missing pattern
#TODO trait override spec
