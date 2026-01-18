Feature: Spec / Trait Overlay Strategy

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """

  Scenario: Spec Overlay - Spec Overlay from Spec Factory / Spec Class / Global Spec Factory / Global Spec Class / Type Factory
    Given the following bean definition:
      """
      public class Bean {
        public String value1, value2, value3, value4, value5;
      }
      """
    And register as follows:
      """
      jFactory.factory(Bean.class).spec(spec -> spec
        .property("value5").value("type-factory"));
      """
    And the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {
        public void main() {
            property("value1").value("spec-class");
        }
      }
      """
    And the following spec definition:
      """
      @Global
      public class GlobalBeanSpec extends Spec<Bean> {
        public void main() {
            property("value3").value("global-spec-class");
        }
      }
      """
    And register as follows:
      """
      jFactory.specFactory(BeanSpec.class).spec(spec -> spec
        .property("value2").value("spec-factory"));
      jFactory.specFactory(GlobalBeanSpec.class).spec(spec -> spec
        .property("value4").value("global-spec-factory"));
      """
    When evaluating the following code:
      """
      jFactory.createAs(BeanSpec.class);
      """
    Then the result should be:
      """
      : {
        value1= spec-class
        value2= spec-factory
        value3= global-spec-class
        value4= global-spec-factory
        value5= type-factory
      }
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

#  Spec precedence
#  Trait precedence
