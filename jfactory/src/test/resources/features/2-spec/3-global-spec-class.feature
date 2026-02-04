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
          property("stringValue").value("global base");
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
      stringValue= 'global base'
      """

  Scenario: Avoid Duplicate Base-Spec Execution - Creating via Global Spec Does Not Collect/Execute Specs Twice
    Given the following bean class:
      """
      public class Bean {
        public int value;
      }
      """
    Given the following spec class:
      """
      @Global
      public class BeanSpec extends Spec<Bean> {
        private static int i = 0;

        @Override
        public void main() {
          property("value").value(i++);
        }
      }
      """
    When build:
      """
      jFactory.spec(BeanSpec.class).create();
      """
    Then the result should:
      """
      value: 0
      """

  Rule: Composition

    Background:
      Given the following bean definition:
      """
      public class Bean {
        public String value1, value2, value3, value4, value5, value6;
      }
      """

    Scenario: Merge Specs from Global-Spec Class and Type-Factory with Trait in Type Creation
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          public void main() {
            property("value2").value("global-spec-class");
          }

          @Trait
          public void globalSpecClassTrait() {
            property("value5").value("global-spec-class-trait");
          }
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class)
          .spec(spec -> spec
            .property("value1").value("type-factory"))
          .spec("typeFactoryTrait", spec -> spec
            .property("value4").value("type-factory-trait"));
        jFactory.specFactory(GlobalBeanSpec.class)
          .spec(spec -> spec
            .property("value3").value("global-spec-factory"))
          .spec("globalSpecFactoryTrait", spec -> spec
            .property("value6").value("global-spec-factory-trait"));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("typeFactoryTrait", "globalSpecClassTrait", "globalSpecFactoryTrait").create();
        """
      Then the result should be:
        """
        : {
          value1= type-factory
          value2= global-spec-class
          value3= global-spec-factory
          value4= type-factory-trait
          value5= global-spec-class-trait
          value6= global-spec-factory-trait
        }
        """

    Scenario: Merge Specs from Spec-Class, Global-Spec Class and Type-Factory in Spec Creation
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2, value3, value4, value5, value6, value7, value8, value9, value10;
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class)
          .spec(spec -> spec
            .property("value5").value("type-factory"))
          .spec("typeFactoryTrait", spec -> spec
            .property("value10").value("type-factory-trait"));
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
              property("value1").value("spec-class");
          }

          @Trait
          public void specClassTrait() {
            property("value6").value("spec-class-trait");
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

          @Trait
          public void globalSpecClassTrait() {
            property("value8").value("global-spec-class-trait");
          }
        }
        """
      And register as follows:
        """
        jFactory.specFactory(BeanSpec.class)
          .spec(spec -> spec
            .property("value2").value("spec-factory"))
          .spec("specFactoryTrait", spec -> spec
            .property("value7").value("spec-factory-trait"));
        jFactory.specFactory(GlobalBeanSpec.class)
          .spec(spec -> spec
            .property("value4").value("global-spec-factory"))
          .spec("globalSpecFactoryTrait", spec -> spec
            .property("value9").value("global-spec-factory-trait"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("typeFactoryTrait", "specClassTrait", "specFactoryTrait", "globalSpecClassTrait", "globalSpecFactoryTrait").create();
        """
      Then the result should be:
        """
        : {
          value1= spec-class
          value2= spec-factory
          value3= global-spec-class
          value4= global-spec-factory
          value5= type-factory
          value6= spec-class-trait
          value7= spec-factory-trait
          value8= global-spec-class-trait
          value9= global-spec-factory-trait
          value10= type-factory-trait
        }
        """

    Scenario: Ineffective Spec - Spec Defined in Non-Global Spec Classes Have No Effect in Type Creation
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

    Scenario: Ineffective Trait - Trait Defined in Non-Global Spec Class Have No Effect in Type Creation
      Given the following spec definition:
        """
        public class NonGlobalBeanSpec extends Spec<Bean> {
          @Trait("trait")
          public void trait1() {
            property("value1").value("any-value");
          }
        }
        """
      And register as follows:
        """
        jFactory.specFactory(NonGlobalBeanSpec.class)
          .spec("trait2", spec -> spec.property("value1").value("any-value"));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("trait1").create();
        """
      Then the result should be:
        """
        ::throw.message= "Trait `trait1` not exist"
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("trait2").create();
        """
      Then the result should be:
        """
        ::throw.message= "Trait `trait2` not exist"
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

    Scenario: Global Spec-Class Trait > Spec-Factory Spec - Middle Priority Trait Takes Precedence over Highest Priority Spec
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          @Trait
          public void trait() {
              property("value").value("global-spec-class-trait");
          }
        }
        """
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.specFactory(BeanSpec.class).spec(spec -> spec
          .property("value").value("spec-factory"));
        jFactory.register(GlobalBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("trait").create();
        """
      Then the result should be:
        """
        value= global-spec-class-trait
        """

    Scenario: Global Spec-Factory Trait > Spec-Factory Spec - Highest Priority Trait Takes Precedence over Highest Priority Spec
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {}
        """
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.specFactory(BeanSpec.class)
          .spec(spec -> spec
            .property("value").value("spec-factory"));
        jFactory.specFactory(GlobalBeanSpec.class)
          .spec("trait", spec -> spec
            .property("value").value("global-spec-factory-trait"));
        """
      When evaluating the following code:
            """
            jFactory.spec(BeanSpec.class).traits("trait").create();
            """
      Then the result should be:
            """
            value= global-spec-factory-trait
            """

  Rule: Trait Precedence

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public String value;
        }
        """

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
