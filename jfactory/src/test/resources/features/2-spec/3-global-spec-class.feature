Feature: Global Spec Class

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    And the following bean definition:
      """
      public class Bean {
        public String stringValue;
        public int intValue;
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

  Scenario: Type Factory Shadowed - Type Factory Returns the Global Spec Class Factory after the Global Spec Class was Registered
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
      jFactory.factory(Bean.class).spec(spec -> spec
        .property("stringValue").value("from_type_factory"));
      """
    When evaluating the following code:
      """
      jFactory.create(Bean.class);
      """
    Then the result should be:
      """
      stringValue= from_type_factory
      """
    And register as follows:
      """
      jFactory.removeGlobalSpec(Bean.class);
      """
    When evaluating the following code:
      """
      jFactory.create(Bean.class);
      """
    Then the result should be:
      """
      stringValue= /^stringValue.*/
      """

  Scenario: Base-Spec Resolution - Base Decided at Create Time, Not at Register Time
    Given the following spec definition:
      """
      public class BeanSpec extends Spec<Bean>{
      }
      """
    And the following spec definition:
      """
      @Global
      public class GlobalBeanSpec extends Spec<Bean>{
        public void main() {
          property("value1").value("global base");
        }
      }
      """
    And register as follows:
      """
      jFactory.register(BeanSpec.class);
      jFactory.register(GlobalBeanSpec.class);
      """
    When evaluating the following code:
      """
      jFactory.spec(BeanSpec.class).create();
      """
    Then the result should be:
      """
      value1= 'global base'
      """

  Rule: Composition

    Background:
      Given the following bean definition:
      """
      public class Bean {
        public String value1, value2, value3, value4, value5;
      }
      """

    Scenario: Merge Specs from Global-Spec Class and Type-Factory in Type Creation
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          public void main() {
            property("value2").value("global-spec-class");
          }
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec(spec -> spec
          .property("value1").value("type-factory"));
        jFactory.specFactory(GlobalBeanSpec.class).spec(spec -> spec
          .property("value3").value("global-spec-factory"));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        : {
          value1= type-factory
          value2= global-spec-class
          value3= global-spec-factory
        }
        """

    Scenario: Merge Specs from Spec-Class, Global-Spec Class and Type-Factory in Spec Creation
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

    Scenario: Ineffective Spec During Type Creation - Spec Defined in Non-Global Spec Classes Have No Effect in Type Creation
      Given the following spec definition:
        """
        public class AnyBeanSpec extends Spec<Bean> {
          public void main() {
            property("value1").value("any-value");
          }
        }
        """
      And register as follows:
        """
        jFactory.specFactory(AnyBeanSpec.class).spec(spec -> spec
          .property("value2").value("any-value"));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        : {
          value1= /value1.*/
          value2= /value2.*/
        }
        """

  Rule: Property Spec Precedence

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public String value;
        }
        """

    Scenario: Global Spec-Class > Type-Factory - Specs in Global Spec Class Takes Precedence over Type Factory Specs
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

    Scenario: Global Spec-Factory > Global Spec-Class - Specs in Global Spec Factory Takes Precedence over Global Spec Class
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

    Scenario: Spec-Class > Global Spec-Factory - Specs in Spec Class Takes Precedence over Global Spec Factory
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
