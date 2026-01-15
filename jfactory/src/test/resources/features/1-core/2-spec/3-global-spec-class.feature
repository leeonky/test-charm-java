Feature: Mark Global Spec Class as the Type Base Spec

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    And the following bean definition:
      """
      public class Bean {
        public String stringValue;
      }
      """

  Scenario: Global Spec Class - Define a Global Spec Class to Apply to All Types
    Given the following spec definition:
      """
      @Global
      public class GlobalBeanSpec extends Spec<Bean> {
        public void main() {
          property("stringValue").value("globalHello");
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
      stringValue= globalHello
      """

  Scenario: Remove Global Spec Class - Remove a Global Spec Class from the Factory
    Given the following spec definition:
      """
      @Global
      public class GlobalBeanSpec extends Spec<Bean> {
        public void main() {
          property("stringValue").value("globalHello");
        }
      }
      """
    And register as follows:
      """
      jFactory.register(GlobalBeanSpec.class);
      jFactory.removeGlobalSpec(Bean.class);
      """
    When evaluating the following code:
      """
      jFactory.type(Bean.class).create();
      """
    Then the result should be:
      """
      stringValue= /^stringValue.*/
      """

  Scenario: Duplicated Global Spec Class - Do not allow Duplicated Global Spec Class Registration
    Given the following spec definition:
      """
      @Global
      public class GlobalBeanSpec1 extends Spec<Bean> {}
      """
    And the following spec definition:
      """
      @Global
      public class GlobalBeanSpec2 extends Spec<Bean> {}
      """
    When register as follows:
      """
      jFactory.register(GlobalBeanSpec1.class);
      jFactory.register(GlobalBeanSpec2.class);
      """
    And evaluating the following code:
      """
      jFactory.createAs(GlobalBeanSpec2.class);
      """
    Then the result should be:
      """
      ::throw.message= "More than one @Global Spec class `GlobalBeanSpec1` and `GlobalBeanSpec2`"
      """

#  Scenario: Type Factory with Global Spec Class - Type Factory is Applied after Global Spec Class
#    Given the following spec definition:
#      """
#      @Global
#      public class GlobalBeanSpec extends Spec<Bean> {
#        public void main() {
#          property("stringValue").value("globalHello");
#        }
#      }
#      """
#    And register as follows:
#      """
#      jFactory.register(GlobalBeanSpec.class);
#      jFactory.factory(Bean.class).spec(spec -> spec
#        .property("stringValue").value("from_type_factory"));
#      """
#    When evaluating the following code:
#      """
#      jFactory.create(Bean.class);
#      """
#    Then the result should be:
#      """
#      stringValue= from_type_factory
#      """


