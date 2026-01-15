Feature: Spec / Trait Overlay Strategry

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """

  Scenario: Spec Overlay - Spec Overlay from Spec Class / Spec Factory / Global Spec Class / Global Spec Factory / Type Factory
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

#    Trait Overlay
#  Spec precedence
#  Trait precedence
